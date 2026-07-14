package com.hmbrandt.job_management_service.service;


import com.hmbrandt.job_management_service.dto.*;
import com.hmbrandt.job_management_service.dto.create.ChangeOrderCreateDto;
import com.hmbrandt.job_management_service.entity.*;
import com.hmbrandt.job_management_service.exception.ResourceNotFoundException;
import com.hmbrandt.job_management_service.repository.ChangeOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChangeOrderServiceImpl implements ChangeOrderService {

    private final ChangeOrderRepository repository;

    @Override
    @Transactional
    public ChangeOrderResponseDTO save(ChangeOrderCreateDto dto) {
        // 1. Obtener el usuario actual para la auditoría interna
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Crear la entidad padre principal (El ID se ignora porque JPA lo generará)
        ChangeOrder changeOrder = new ChangeOrder();
        changeOrder.setJobId(dto.jobId());
        changeOrder.setEmployeeId(dto.employeeId());
        changeOrder.setOrderDate(dto.orderDate());
        changeOrder.setOrderNumber(dto.orderNumber());
        changeOrder.setAmount(dto.amount());
        changeOrder.setOrderStatus(dto.orderStatus());
        changeOrder.setCreatedBy(currentUser);
        changeOrder.setUpdatedBy(currentUser);

        // 3. Mapear la lista de Tareas si el DTO las incluye
        if (dto.tasks() != null) {
            List<OrderTask> tasks = dto.tasks().stream().map(taskDto -> {
                OrderTask task = new OrderTask();
                task.setTaskName(taskDto.taskName());
                task.setTaskDescription(taskDto.taskDescription());
                task.setForeman(taskDto.foreman());
                task.setLabor(taskDto.labor());
                task.setOther(taskDto.other());
                task.setTotalHours(taskDto.totalHours());
                task.setComments(taskDto.comments());
                task.setEquipmentComments(taskDto.equipmentComments());
                task.setToolComments(taskDto.toolComments());
                task.setDumpsterComments(taskDto.dumpsterComments());

                // ASIGNACIÓN CLAVE: Enlazamos el hijo con su padre e inyectamos auditoría
                task.setChangeOrder(changeOrder);
                task.setCreatedBy(currentUser);
                task.setUpdatedBy(currentUser);

                // 3a. Mapear Equipos de esta tarea
                if (taskDto.equipments() != null) {
                    List<TaskEquipment> equipments = taskDto.equipments().stream().map(equipDto -> {
                        TaskEquipment equip = new TaskEquipment();
                        equip.setEquipmentName(equipDto.equipmentName());
                        equip.setQuantity(equipDto.quantity());
                        equip.setCreatedBy(currentUser);
                        equip.setOrderTask(task);
                        return equip;
                    }).toList();
                    task.setEquipments(equipments);
                }

                // 3b. Mapear Herramientas (Tools) de esta tarea
                if (taskDto.tools() != null) {
                    List<TaskTool> tools = taskDto.tools().stream().map(toolDto -> {
                        TaskTool tool = new TaskTool();
                        tool.setToolName(toolDto.toolName());
                        tool.setQuantity(toolDto.quantity());
                        tool.setCreatedBy(currentUser);
                        tool.setOrderTask(task);
                        return tool;
                    }).toList();
                    task.setTools(tools);
                }

                // 3c. Mapear Volquetes (Dumpsters) de esta tarea
                if (taskDto.dumpsters() != null) {
                    List<TaskDumpster> dumpsters = taskDto.dumpsters().stream().map(dumpDto -> {
                        TaskDumpster dumpster = new TaskDumpster();
                        dumpster.setMaterialType(dumpDto.materialType());
                        dumpster.setDumpsterSize(dumpDto.dumpsterSize());
                        dumpster.setQuantity(dumpDto.quantity());
                        dumpster.setCreatedBy(currentUser);
                        dumpster.setOrderTask(task);
                        return dumpster;
                    }).toList();
                    task.setDumpsters(dumpsters);
                }

                return task;
            }).toList();

            changeOrder.setTasks(tasks);
        }

        if (dto.signatures() != null) {
            // 1. Obtenemos la lista actual de firmas de la orden (inicializándola si es null)
            List<OrderSignature> existingSignatures = changeOrder.getSignatures();
            if (existingSignatures == null) {
                existingSignatures = new ArrayList<>();
                changeOrder.setSignatures(existingSignatures);
            }

            for (var sigDto : dto.signatures()) {
                // Buscamos si ya existe una firma guardada para este rol específico
                Optional<OrderSignature> existingSigOpt = existingSignatures.stream()
                        .filter(sig -> sig.getSignatureRole().equals(sigDto.signatureRole()))
                        .findFirst();

                if (existingSigOpt.isPresent()) {
                    // --- CASO A: LA FIRMA YA EXISTÍA ---
                    OrderSignature existingSig = existingSigOpt.get();

                    // Solo si nos mandan una firma nueva en Base64 (signatureData no es null/vacío), la actualizamos
                    if (sigDto.signatureData() != null && !sigDto.signatureData().isBlank()) {
                        // Opcional: Aquí podrías eliminar el archivo físico anterior del disco del VPS si lo deseas
                        // deleteFileFromDisk(existingSig.getFilePath());

                        String storedPath = saveSignatureToDisk(sigDto.signatureData());
                        existingSig.setFilePath(storedPath);
                        existingSig.setSignatureName(sigDto.signatureName());
                        existingSig.setCreatedBy(currentUser); // Opcional actualizar quién la modificó
                    }
                    // Si viene en el DTO pero sin signatureData (ej. solo pasamos el filePath existente desde el front),
                    // no hacemos nada para no pisarla con un archivo vacío.

                } else {
                    // --- CASO B: ES UNA FIRMA NUEVA ---
                    // Solo creamos la entidad si realmente nos están mandando datos para firmar
                    if (sigDto.signatureData() != null && !sigDto.signatureData().isBlank()) {
                        OrderSignature newSignature = new OrderSignature();
                        newSignature.setSignatureRole(sigDto.signatureRole());
                        newSignature.setSignatureName(sigDto.signatureName());

                        String storedPath = saveSignatureToDisk(sigDto.signatureData());
                        newSignature.setFilePath(storedPath);

                        newSignature.setChangeOrder(changeOrder);
                        newSignature.setCreatedBy(currentUser);

                        // Agregamos a la lista existente sin romper la referencia de Hibernate
                        existingSignatures.add(newSignature);
                    }
                }
            }
        }

        ChangeOrder savedOrder = repository.save(changeOrder);

        return mapToDto(savedOrder);
    }

    @Override
    @Transactional
    public ChangeOrderResponseDTO finalizeOrder(Long orderId){
        String currentUser = "SYSTEM_FALLBACK"; // Valor por defecto por si falla

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = authentication.getName();
        } else {
            currentUser = "SYSTEM_FALLBACK";
            System.out.println(">>> ALERTA: La petición llegó SIN autenticación o el contexto es NULL");
        }

        ChangeOrder order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order not found with id: " + orderId));

        order.setOrderStatus("FINALIZED");
        order.setUpdatedBy(currentUser);

        repository.save(order);
        return mapToDto(order);
    }

    @Override
    @Transactional
    public ChangeOrderResponseDTO approveOrder(Long orderId) {
        String currentUser = "SYSTEM_FALLBACK";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = authentication.getName();
        } else {
            System.out.println(">>> ALERTA: La petición de aprobación llegó sin autenticación");
        }

        ChangeOrder order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Opcional: Regla de negocio para evitar aprobaciones si no está en DRAFT
        if (!"DRAFT".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Only DRAFT orders can be approved.");
        }

        order.setOrderStatus("APPROVED");
        order.setUpdatedBy(currentUser);

        repository.save(order);
        return mapToDto(order);
    }

    @Override
    @Transactional
    public ChangeOrderResponseDTO update(Long id, ChangeOrderUpdateDTO dto) {
        // 1. Buscar la entidad existente o lanzar error si no existe
        ChangeOrder order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found with id: " + id));

        String currentUser; // Valor por defecto por si falla

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = authentication.getName();
            System.out.println(">>> USUARIO AUTENTICADO ENCONTRADO: " + currentUser);
        } else {
            currentUser = "SYSTEM_FALLBACK";
            System.out.println(">>> ALERTA: La petición llegó SIN autenticación o el contexto es NULL");
        }

        if ("FINALIZED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Locked documents cannot be updated");
        }

        order.setEmployeeId(dto.employeeId());
        order.setOrderDate(dto.orderDate());
        order.setOrderNumber(dto.orderNumber());
        order.setAmount(dto.amount());
        order.setOrderStatus(dto.orderStatus());
        order.setUpdatedBy(currentUser);
        order.setUpdatedAt(LocalDateTime.now());

        if (dto.tasks() != null) {
            for (OrderTask existingTask : order.getTasks()) {
                boolean updateTask = dto.tasks().stream()
                        .anyMatch(t -> t.id() != null && t.id().equals(existingTask.getId()));

                if (!updateTask) {
                    existingTask.setDeletedAt(LocalDateTime.now());
                    existingTask.setUpdatedBy(currentUser);

                    if (existingTask.getEquipments() != null) {
                        existingTask.getEquipments().forEach(eq -> eq.setDeletedAt(LocalDateTime.now()));
                    }

                    if (existingTask.getTools() != null) {
                        existingTask.getTools().forEach(eq -> eq.setDeletedAt(LocalDateTime.now()));
                    }

                    if (existingTask.getDumpsters() != null) {
                        existingTask.getDumpsters().forEach(eq -> eq.setDeletedAt(LocalDateTime.now()));
                    }
                }
            }

            dto.tasks().forEach(taskDto -> {

                if (taskDto.id() == null) {
                    OrderTask newTaskEntity = mapTaskToEntity(taskDto);
                    newTaskEntity.setChangeOrder(order);
                    newTaskEntity.setCreatedBy(currentUser);
                    newTaskEntity.setUpdatedBy(currentUser);
                    order.getTasks().add(newTaskEntity);
                } else {
                    OrderTask taskToUpdate = order.getTasks().stream()
                            .filter(t -> t.getId().equals(taskDto.id()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

                    taskToUpdate.setTaskName(taskDto.taskName());
                    taskToUpdate.setTaskDescription(taskDto.taskDescription());
                    taskToUpdate.setForeman(taskDto.foreman());
                    taskToUpdate.setLabor(taskDto.labor());
                    taskToUpdate.setOther(taskDto.other());
                    taskToUpdate.setTotalHours(taskDto.totalHours());
                    taskToUpdate.setComments(taskDto.comments());
                    taskToUpdate.setEquipmentComments(taskDto.equipmentComments());
                    taskToUpdate.setToolComments(taskDto.toolComments());
                    taskToUpdate.setDumpsterComments(taskDto.dumpsterComments());

                    taskToUpdate.setUpdatedBy(currentUser);
                    taskToUpdate.setUpdatedAt(LocalDateTime.now());

                    if (taskDto.equipments() != null) {
                        taskToUpdate.getEquipments().clear(); // Limpia los viejos en memoria
                        taskDto.equipments().forEach(eqDto -> {
                            TaskEquipment eqEntity = mapEquipmentToEntity(eqDto, currentUser);
                            eqEntity.setOrderTask(taskToUpdate);
                            taskToUpdate.getEquipments().add(eqEntity);
                        });
                    }

                    // 2. Herramientas
                    if (taskDto.tools() != null) {
                        taskToUpdate.getTools().clear();
                        taskDto.tools().forEach(toolDto -> {
                            TaskTool toolEntity = mapToolToEntity(toolDto,currentUser);
                            toolEntity.setOrderTask(taskToUpdate);
                            taskToUpdate.getTools().add(toolEntity);
                        });
                    }

                    // 3. Contenedores
                    if (taskDto.dumpsters() != null) {
                        taskToUpdate.getDumpsters().clear();
                        taskDto.dumpsters().forEach(dumpDto -> {
                            TaskDumpster dumpEntity = mapDumpsterToEntity(dumpDto, currentUser);
                            dumpEntity.setOrderTask(taskToUpdate);
                            taskToUpdate.getDumpsters().add(dumpEntity);
                        });
                    }
                }
            });

        }

        // ==========================================
        // NUEVA SECCIÓN: MANEJO DE FIRMAS EN EL UPDATE
        // ==========================================
        if (dto.signatures() != null) {
            // Opción recomendada: Limpiar firmas viejas para evitar duplicados o huérfanas
            if (order.getSignatures() != null) {
                order.getSignatures().clear();
            } else {
                order.setSignatures(new ArrayList<>());
            }

            List<OrderSignature> newSignatures = dto.signatures().stream().map(sigDto -> {
                OrderSignature signature = new OrderSignature();
                signature.setSignatureRole(sigDto.signatureRole());
                signature.setSignatureName(sigDto.signatureName());

                // 1. Guardamos el Base64 que viene en el UpdateDTO al disco del VPS
                String storedPath = saveSignatureToDisk(sigDto.signatureData());

                // 2. Seteamos la ruta física del archivo
                signature.setFilePath(storedPath);
                signature.setChangeOrder(order);
                signature.setCreatedBy(currentUser);
                return signature;
            }).toList();

            // 3. Asignamos las nuevas firmas procesadas a la orden
            order.getSignatures().addAll(newSignatures);
        }
        // ==========================================

        ChangeOrder savedOrder = repository.save(order);
        return mapToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeOrderResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto) // Uso de Method Reference para limpiar el código
                .orElseThrow(() -> new EntityNotFoundException("Order not fount with id xf001: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeOrderResponseDTO> findByJobId(Long jobId) {
        return repository.findByJobId(jobId).stream()
                .map(this::mapToDto) // Uso de Method Reference para limpiar el código
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ID not found.");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeOrderResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Value("${application.upload.dir}")
    private String uploadDir;

    private String saveSignatureToDisk(String base64Image) {
        try {
            // 1. Limpiar el prefijo de Base64 si React lo envía completo (data:image/png;base64,...)
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",")[1];
            }

            // 2. Decodificar los bytes de la imagen
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            // 3. Definir la ruta del VPS (¡Usa variables de configuración en tu application.yml!)
            // Ejemplo local windows/mac o absoluto en linux del VPS: "/var/www/uploads/signatures/"
//            String uploadDir = "/var/www/uploads/signatures/";
//            String uploadDir = "C:/hmbrandt/uploads/signatures/";
//
//            File folder = new File(uploadDir);
//            if (!folder.exists()) {
//                folder.mkdirs(); // Crea la carpeta en el VPS si no existe
//            }
            Path directoryPath = Paths.get(uploadDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath); // Crea las carpetas necesarias en C:/ o /var/
            }

            // 4. Crear un nombre de archivo único para evitar duplicados
            String fileName = UUID.randomUUID().toString() + ".png";
            Path targetFilePath = directoryPath.resolve(fileName);
//            File fileToSave = new File(folder, fileName);

            // 5. Escribir los bytes en el archivo físico
//            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
//                fos.write(imageBytes);
//            }
            Files.write(targetFilePath, imageBytes);

            // 6. Retornar el path relativo o el nombre que guardarás en la BD
            return "/uploads/signatures/" + fileName;

        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Issue savin signature in file system: " + e.getMessage());
        }
    }

    private TaskEquipment mapEquipmentToEntity(TaskEquipmentResponseDto dto, String currentUser){
        return TaskEquipment.builder()
                .id(dto.id())
                .equipmentName(dto.equipmentName())
                .quantity(dto.quantity())
                .createdBy(currentUser)
                .build();
    }

    private TaskTool mapToolToEntity(TaskToolResponseDto dto, String currentUser){
        return TaskTool.builder()
                .id(dto.id())
                .toolName(dto.toolName())
                .quantity(dto.quantity())
                .createdBy(currentUser)
                .build();
    }

    private TaskDumpster mapDumpsterToEntity(TaskDumpsterResponseDto dto, String currentUser){
        return TaskDumpster.builder()
                .id(dto.id())
                .materialType(dto.materialType())
                .dumpsterSize(dto.dumpsterSize())
                .quantity(dto.quantity())
                .createdBy(currentUser)
                .build();
    }

    private OrderTask mapTaskToEntity(OrderTaskResponseDto dto) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        OrderTask task = new OrderTask();

        task.setId(dto.id());
        task.setTaskName(dto.taskName());
        task.setTaskDescription(dto.taskDescription());
        task.setForeman(dto.foreman());
        task.setLabor(dto.labor());
        task.setOther(dto.other());
        task.setTotalHours(dto.totalHours());
        task.setComments(dto.comments());
        if(dto.equipments() != null){
            List<TaskEquipment> equipmentEntities = dto.equipments().stream()
                    .map(eqDto -> {
                        TaskEquipment eq = mapEquipmentToEntity(eqDto, currentUser);
                        eq.setOrderTask(task);
                        return eq;
                    })
                    .collect(Collectors.toList());

            task.setEquipments(equipmentEntities);
        }
        if(dto.tools() != null){
            List<TaskTool> toolEntities = dto.tools().stream()
                    .map(dtoItem -> {
                        TaskTool item =  mapToolToEntity(dtoItem, currentUser);
                        item.setOrderTask(task);
                        return item;
                    })
                    .collect(Collectors.toList());

            task.setTools(toolEntities);
        }
        if(dto.dumpsters() != null){
            List<TaskDumpster> dumpsterEntities = dto.dumpsters().stream()
                    .map(dtoItem -> {
                        TaskDumpster item =  mapDumpsterToEntity(dtoItem, currentUser);
                        item.setOrderTask(task);
                        return item;
                    })
                    .collect(Collectors.toList());

            task.setDumpsters(dumpsterEntities);
        }
        task.setUpdatedBy(currentUser);
        task.setUpdatedAt(LocalDateTime.now());

        return task;
    }

    private ChangeOrderResponseDTO mapToDto(ChangeOrder entity) {
        return new ChangeOrderResponseDTO(
                entity.getId(),
                entity.getJobId(),
                entity.getEmployeeId(),
                entity.getOrderDate(),
                entity.getOrderNumber(),
                entity.getAmount(),
                entity.getOrderStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getTasks()
                        .stream()
                        .map(this::mapTaskToDto)
                        .collect(Collectors.toList()),
                entity.getSignatures()
                        .stream()
                        .map(this::mapSignatureToDto)
                        .collect(Collectors.toList())

        );
    }

    private OrderTaskResponseDto mapTaskToDto(OrderTask entity){
        return new OrderTaskResponseDto(
                entity.getId(),
                entity.getTaskName(),
                entity.getTaskDescription(),
                entity.getForeman(),
                entity.getLabor(),
                entity.getOther(),
                entity.getTotalHours(),
                entity.getComments(),
                entity.getEquipmentComments(),
                entity.getToolComments(),
                entity.getDumpsterComments(),
                entity.getEquipments()
                        .stream()
                        .map(this::mapEquipmentToDto)
                        .collect(Collectors.toList()),
                entity.getTools()
                        .stream()
                        .map(this::mapToolToDto)
                        .collect(Collectors.toList()),
                entity.getDumpsters()
                        .stream()
                        .map(this::mapDumpsterToDto)
                        .collect(Collectors.toList())

        );
    }

    private TaskEquipmentResponseDto mapEquipmentToDto(TaskEquipment entity){
        return new TaskEquipmentResponseDto(
                entity.getId(),
                entity.getEquipmentName(),
                entity.getQuantity()
        );
    }

    private TaskToolResponseDto mapToolToDto(TaskTool entity){
        return new TaskToolResponseDto(
                entity.getId(),
                entity.getToolName(),
                entity.getQuantity()
        );
    }

    private TaskDumpsterResponseDto mapDumpsterToDto(TaskDumpster entity){
        return new TaskDumpsterResponseDto(
                entity.getId(),
                entity.getMaterialType(),
                entity.getDumpsterSize(),
                entity.getQuantity()
        );
    }

    @Value("${application.base-url}") // http://localhost:8080 en PC, https://api-gateway-px44.onrender.com en VPS
    private String baseUrl;

    private OrderSignatureResponseDto mapSignatureToDto(OrderSignature entity){
        String filePath = entity.getFilePath();
        String fullUrl = baseUrl;

        if (filePath != null) {
            if (filePath.startsWith("/") && baseUrl.endsWith("/")) {
                fullUrl = baseUrl + filePath.substring(1);
            } else if (!filePath.startsWith("/") && !baseUrl.endsWith("/")) {
                fullUrl = baseUrl + "/" + filePath;
            } else {
                fullUrl = baseUrl + filePath;
            }
        }

        return new OrderSignatureResponseDto(
                entity.getId(),
                entity.getSignatureRole(),
                fullUrl,
                entity.getSignatureName()
        );
    }

}
