package com.hmbrandt.job_management_service.service;

import com.hmbrandt.job_management_service.dto.schedule.AbsenceResponse;
import com.hmbrandt.job_management_service.dto.schedule.CreateAbsenceRequest;
import com.hmbrandt.job_management_service.dto.schedule.CreateAbsenceResponse;
import com.hmbrandt.job_management_service.dto.schedule.UpdatedScheduleResponse;
import com.hmbrandt.job_management_service.entity.schedule.AbsenceType;
import com.hmbrandt.job_management_service.entity.schedule.EmployeeAbsence;
import com.hmbrandt.job_management_service.entity.schedule.JobSchedule;
import com.hmbrandt.job_management_service.exception.ResourceNotFoundException;
import com.hmbrandt.job_management_service.exception.UnprocessableEntityException;
import com.hmbrandt.job_management_service.repository.schedule.EmployeeAbsenceRepository;
import com.hmbrandt.job_management_service.repository.schedule.JobScheduleEmployeeRepository;
import com.hmbrandt.job_management_service.repository.schedule.JobScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbsenceService {
    private final EmployeeAbsenceRepository absenceRepository;
    private final JobScheduleEmployeeRepository scheduleEmployeeRepository;
    private final JobScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<AbsenceResponse> getAbsences(String fromStr, String toStr) {
        if (fromStr == null || fromStr.isBlank() || toStr == null || toStr.isBlank()) {
            throw new UnprocessableEntityException("Los parámetros 'from' y 'to' son obligatorios.");
        }

        LocalDate from = parseDateOr422(fromStr, "from");
        LocalDate to = parseDateOr422(toStr, "to");

        if (from.isAfter(to)) {
            throw new UnprocessableEntityException("La fecha 'from' no puede ser posterior a 'to'.");
        }

        return absenceRepository.findByDateBetweenOrderByDateAscIdAsc(from, to)
                .stream()
                .map(AbsenceResponse::from)
                .toList();
    }

    @Transactional
    public CreateAbsenceResponse createAbsence(CreateAbsenceRequest request) {
        if (request == null || request.employeesId() == null) {
            throw new UnprocessableEntityException("employeesId es obligatorio.");
        }

        LocalDate date = parseDateOr422(request.date(), "date");
        AbsenceType type = AbsenceType.fromString(request.absenceType());

        // TODO: Validar contra el microservicio de Employees que el empleado exista
        // y que su estado no sea "Terminated" (lanzar UnprocessableEntityException si aplica).

        if (absenceRepository.existsByEmployeesIdAndDate(request.employeesId(), date)) {
            throw new UnprocessableEntityException("El empleado ya tiene una ausencia registrada en esa fecha.");
        }

        // 1. INSERT de la ausencia
        EmployeeAbsence absence = new EmployeeAbsence();
        absence.setEmployeesId(request.employeesId());
        absence.setDate(date);
        absence.setAbsenceType(type);
        absence.setAbsenceComment(request.absenceComment());

        try {
            absence = absenceRepository.saveAndFlush(absence);
        } catch (DataIntegrityViolationException ex) {
            if (hasConstraint(ex, "uq_employee_absences_day")) {
                throw new UnprocessableEntityException("El empleado ya tiene una ausencia registrada en esa fecha.");
            }
            throw ex;
        }

        // 2. Soft delete en job_schedule_employees uniendo con job_schedules por scheduled_date
        scheduleEmployeeRepository.softDeleteEmployeeFromSchedulesByDate(request.employeesId(), date);

        // 3. Leer programaciones de ese día para armar updatedSchedules
        List<UpdatedScheduleResponse> updatedSchedules = scheduleRepository
                .findByScheduledDateOrderByStartTimeAscIdAsc(date)
                .stream()
                .map(this::toUpdatedScheduleResponse)
                .toList();

        return new CreateAbsenceResponse(AbsenceResponse.from(absence), updatedSchedules);
    }

    @Transactional
    public void deleteAbsence(Long id) {
        EmployeeAbsence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ausencia no encontrada con id: " + id));
        absenceRepository.delete(absence);
    }

    private UpdatedScheduleResponse toUpdatedScheduleResponse(JobSchedule schedule) {
        return new UpdatedScheduleResponse(
                schedule.getId(),
                schedule.getJobId(),
                schedule.getScheduledDate(),
                schedule.getStartTime(),
                schedule.getScheduleComment(),
                List.copyOf(schedule.getEmployeeIds())
        );
    }

    private LocalDate parseDateOr422(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new UnprocessableEntityException("El campo '" + fieldName + "' es obligatorio.");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new UnprocessableEntityException("Formato de fecha inválido en '" + fieldName + "'. Use YYYY-MM-DD.");
        }
    }

    private boolean hasConstraint(Throwable ex, String constraintName) {
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(constraintName)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
