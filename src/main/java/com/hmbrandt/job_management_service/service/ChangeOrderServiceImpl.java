package com.hmbrandt.job_management_service.service;


import com.hmbrandt.job_management_service.dto.*;
import com.hmbrandt.job_management_service.dto.create.ChangeOrderCreateDto;
import com.hmbrandt.job_management_service.entity.*;
import com.hmbrandt.job_management_service.exception.ResourceNotFoundException;
import com.hmbrandt.job_management_service.repository.ChangeOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

        // 4. Mapear las Firmas (Signatures)
        if (dto.signatures() != null) {
            List<OrderSignature> signatures = dto.signatures().stream().map(sigDto -> {
                OrderSignature signature = new OrderSignature();
                signature.setSignatureRole(sigDto.signatureRole());
                signature.setFilePath(sigDto.filePath());
                signature.setChangeOrder(changeOrder);
                signature.setCreatedBy(currentUser);
                return signature;
            }).toList();

            changeOrder.setSignatures(signatures);
        }

        ChangeOrder savedOrder = repository.save(changeOrder);

        return mapToDto(savedOrder);
    }



    @Override
    @Transactional
    public ChangeOrderResponseDTO update(Long id, ChangeOrderUpdateDTO dto) {
        // 1. Buscar la entidad existente o lanzar error si no existe
        ChangeOrder order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found with id: " + id));

        if ("SIGNED".equals(order.getOrderStatus()) || "EXECUTED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Locked documents cannot be updated");
        }

        order.setEmployeeId(dto.employeeId());
        order.setOrderDate(dto.orderDate());
        order.setOrderNumber(dto.orderNumber());
        order.setAmount(dto.amount());
        order.setOrderStatus(dto.orderStatus());
        order.setUpdatedBy(dto.updatedBy());
        order.setUpdatedAt(LocalDateTime.now());

        if (dto.tasks() != null) {
            for (OrderTask existingTask : order.getTasks()) {
                boolean updateTask = dto.tasks().stream()
                        .anyMatch(t -> t.id() != null && t.id().equals(existingTask.getId()));

                if (!updateTask) {
                    existingTask.setDeletedAt(LocalDateTime.now());
                    existingTask.setUpdatedBy(dto.updatedBy());

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
                    newTaskEntity.setCreatedBy(dto.updatedBy());
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

                    taskToUpdate.setUpdatedBy(dto.updatedBy());
                    taskToUpdate.setUpdatedAt(LocalDateTime.now());

                    if (taskDto.equipments() != null) {
                        taskToUpdate.getEquipments().clear(); // Limpia los viejos en memoria
                        taskDto.equipments().forEach(eqDto -> {
                            TaskEquipment eqEntity = mapEquipmentToEntity(eqDto);
                            eqEntity.setOrderTask(taskToUpdate); // Plumbing
                            taskToUpdate.getEquipments().add(eqEntity);
                        });
                    }

                    // 2. Herramientas
                    if (taskDto.tools() != null) {
                        taskToUpdate.getTools().clear();
                        taskDto.tools().forEach(toolDto -> {
                            TaskTool toolEntity = mapToolToEntity(toolDto);
                            toolEntity.setOrderTask(taskToUpdate);
                            taskToUpdate.getTools().add(toolEntity);
                        });
                    }

                    // 3. Contenedores
                    if (taskDto.dumpsters() != null) {
                        taskToUpdate.getDumpsters().clear();
                        taskDto.dumpsters().forEach(dumpDto -> {
                            TaskDumpster dumpEntity = mapDumpsterToEntity(dumpDto);
                            dumpEntity.setOrderTask(taskToUpdate);
                            taskToUpdate.getDumpsters().add(dumpEntity);
                        });
                    }
                }
            });

        }

        ChangeOrder savedOrder = repository.save(order);
        return mapToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeOrderResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto) // Uso de Method Reference para limpiar el código
                .orElseThrow(() -> new EntityNotFoundException("Order not fount with id: " + id));
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

    private TaskEquipment mapEquipmentToEntity(TaskEquipmentResponseDto dto){
        return TaskEquipment.builder()
                .id(dto.id())
                .equipmentName(dto.equipmentName())
                .quantity(dto.quantity())
                .build();
    }

    private TaskTool mapToolToEntity(TaskToolResponseDto dto){
        return TaskTool.builder()
                .id(dto.id())
                .toolName(dto.toolName())
                .quantity(dto.quantity())
                .build();
    }

    private TaskDumpster mapDumpsterToEntity(TaskDumpsterResponseDto dto){
        return TaskDumpster.builder()
                .id(dto.id())
                .materialType(dto.materialType())
                .dumpsterSize(dto.dumpsterSize())
                .quantity(dto.quantity())
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
                        TaskEquipment eq = mapEquipmentToEntity(eqDto);
                        eq.setOrderTask(task);
                        return eq;
                    })
                    .collect(Collectors.toList());

            task.setEquipments(equipmentEntities);
        }
        if(dto.tools() != null){
            List<TaskTool> toolEntities = dto.tools().stream()
                    .map(dtoItem -> {
                        TaskTool item =  mapToolToEntity(dtoItem);
                        item.setOrderTask(task);
                        return item;
                    })
                    .collect(Collectors.toList());

            task.setTools(toolEntities);
        }
        if(dto.dumpsters() != null){
            List<TaskDumpster> dumpsterEntities = dto.dumpsters().stream()
                    .map(dtoItem -> {
                        TaskDumpster item =  mapDumpsterToEntity(dtoItem);
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

    private OrderSignatureResponseDto mapSignatureToDto(OrderSignature entity){
        return new OrderSignatureResponseDto(
                entity.getId(),
                entity.getSignatureRole(),
                entity.getFilePath()
        );
    }

}
