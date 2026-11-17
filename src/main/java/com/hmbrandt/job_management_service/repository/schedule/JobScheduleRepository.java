package com.hmbrandt.job_management_service.repository.schedule;

import com.hmbrandt.job_management_service.entity.schedule.JobSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobScheduleRepository extends JpaRepository<JobSchedule, Long> {
    // 1. Buscar un schedule por ID trayendo sus employeeIds en una sola consulta (evita LazyInitializationException)
    @EntityGraph(attributePaths = {"employeeIds"})
    Optional<JobSchedule> findWithEmployeesById(Long id);

    // 2. Aprovecha el índice idx_job_schedules_date (scheduled_date, start_time)
    @EntityGraph(attributePaths = {"employeeIds"})
    List<JobSchedule> findByScheduledDateOrderByStartTimeAsc(LocalDate scheduledDate);

    // 3. Buscar por rango de fechas (ideal para vistas de calendario semanal/mensual)
    @EntityGraph(attributePaths = {"employeeIds"})
    List<JobSchedule> findByScheduledDateBetweenOrderByScheduledDateAscStartTimeAsc(
            LocalDate startDate,
            LocalDate endDate
    );

    // 4. Buscar todos los schedules asociados a un trabajo específico
    @EntityGraph(attributePaths = {"employeeIds"})
    List<JobSchedule> findByJobIdOrderByScheduledDateAscStartTimeAsc(Integer jobId);

    // 5. Aprovecha el índice idx_jse_employee: busca agendas donde participe un empleado
    @Query("SELECT DISTINCT js FROM JobSchedule js LEFT JOIN FETCH js.employeeIds WHERE :employeeId MEMBER OF js.employeeIds ORDER BY js.scheduledDate ASC, js.startTime ASC")
    List<JobSchedule> findAllByEmployeeId(@Param("employeeId") Integer employeeId);

    // 6. Buscar agendas de un empleado en una fecha específica (cruce de ambos índices)
    @Query("SELECT DISTINCT js FROM JobSchedule js LEFT JOIN FETCH js.employeeIds WHERE :employeeId MEMBER OF js.employeeIds AND js.scheduledDate = :date ORDER BY js.startTime ASC")
    List<JobSchedule> findByEmployeeIdAndScheduledDate(
            @Param("employeeId") Integer employeeId,
            @Param("date") LocalDate date
    );

    // 7. Verificar si un empleado ya tiene algo agendado en cierta fecha (útil para validaciones)
    boolean existsByEmployeeIdsContainingAndScheduledDate(Integer employeeId, LocalDate scheduledDate);
}
