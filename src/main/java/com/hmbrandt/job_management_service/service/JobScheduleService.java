package com.hmbrandt.job_management_service.service;

import com.hmbrandt.job_management_service.dto.schedule.CreateScheduleRequest;
import com.hmbrandt.job_management_service.dto.schedule.MoveEmployeeRequest;
import com.hmbrandt.job_management_service.dto.schedule.PatchScheduleRequest;
import com.hmbrandt.job_management_service.dto.schedule.ScheduleResponse;
import com.hmbrandt.job_management_service.entity.schedule.JobSchedule;
import com.hmbrandt.job_management_service.repository.schedule.JobScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobScheduleService {
    private final JobScheduleRepository repository;

    // 1. Arrastrar del catálogo a un día
    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {

        String userName = getUser();

        JobSchedule schedule = JobSchedule.builder()
                .jobId(request.jobId())
                .scheduledDate(request.date())
                .startTime(request.startTime())
                .scheduleComment(request.scheduleComment())
                .createdBy(userName)
                .employeeIds(request.employeeIds() != null
                        ? new HashSet<>(request.employeeIds())
                        : new HashSet<>())
                .build();

        return toResponse(repository.save(schedule));
    }

    // 2 y 3. Mover tarjeta a otro día y/o cambiar la hora (Actualización parcial)
    @Transactional
    public ScheduleResponse patchSchedule(Long id, PatchScheduleRequest request) {
        JobSchedule schedule = findOrThrow(id);

        if (request.date() != null) {
            schedule.setScheduledDate(request.date());
        }
        if (request.startTime() != null) {
            schedule.setStartTime(request.startTime());
        }
        if (request.scheduleComment() != null) {
            schedule.setScheduleComment(request.scheduleComment());
        }

        return toResponse(repository.save(schedule));
    }

    // 4. Quitar tarjeta (soltar sobre el catálogo)
    @Transactional
    public void deleteSchedule(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Schedule no encontrado con id: " + id);
        }
        // Elimina automáticamente los registros de job_schedule_employees
        repository.deleteById(id);
    }

    // 5. Asignar o quitar empleados (reemplaza el conjunto completo)
    @Transactional
    public ScheduleResponse updateEmployees(Long id, Set<Integer> employeeIds) {
        JobSchedule schedule = findOrThrow(id);

        schedule.getEmployeeIds().clear();
        if (employeeIds != null) {
            schedule.getEmployeeIds().addAll(employeeIds);
        }

        return toResponse(repository.save(schedule));
    }

    // 6. Mover empleado entre dos tarjetas en una sola transacción atómica
    @Transactional
    public List<ScheduleResponse> moveEmployeeBetweenSchedules(MoveEmployeeRequest request) {
        if (request.sourceScheduleId().equals(request.targetScheduleId())) {
            throw new IllegalArgumentException("La tarjeta de origen y destino no pueden ser la misma");
        }

        JobSchedule source = findOrThrow(request.sourceScheduleId());
        JobSchedule target = findOrThrow(request.targetScheduleId());

        source.getEmployeeIds().remove(request.employeeId());
        target.getEmployeeIds().add(request.employeeId());

        repository.save(source);
        repository.save(target);

        return List.of(toResponse(source), toResponse(target));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("La fecha 'from' no puede ser posterior a la fecha 'to'");
        }

        return repository
                .findByScheduledDateBetweenOrderByScheduledDateAscStartTimeAsc(from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private JobSchedule findOrThrow(Long id) {
        return repository.findWithEmployeesById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule no encontrado con id: " + id));
    }

    private ScheduleResponse toResponse(JobSchedule entity) {
        return new ScheduleResponse(
                entity.getId(),
                entity.getJobId(),
                entity.getScheduledDate(),
                entity.getStartTime(),
                entity.getScheduleComment(),
                Set.copyOf(entity.getEmployeeIds()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String getUser(){
        String currentUser = "SYSTEM_FALLBACK";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = authentication.getName();
        } else {
            System.out.println(">>> ALERT: no authenticated or context NULL");
        }

        return currentUser;
    }


}
