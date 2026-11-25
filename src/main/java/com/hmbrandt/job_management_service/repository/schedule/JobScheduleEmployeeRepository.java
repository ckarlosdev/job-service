package com.hmbrandt.job_management_service.repository.schedule;

import com.hmbrandt.job_management_service.entity.schedule.JobScheduleEmployee;
import com.hmbrandt.job_management_service.entity.schedule.JobScheduleEmployeeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface JobScheduleEmployeeRepository extends JpaRepository<JobScheduleEmployee, JobScheduleEmployeeId> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE job_schedule_employees jse
        INNER JOIN job_schedules js ON jse.job_schedule_id = js.id
        SET jse.deleted_at = NOW()
        WHERE jse.employee_id = :employeeId
          AND js.scheduled_date = :date
          AND js.deleted_at IS NULL
          AND jse.deleted_at IS NULL
        """, nativeQuery = true)
    int softDeleteEmployeeFromSchedulesByDate(
            @Param("employeeId") Integer employeeId,
            @Param("date") LocalDate date
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE job_schedule_employees
    SET deleted_at = NOW()
    WHERE job_schedule_id = :scheduleId
      AND deleted_at IS NULL
    """, nativeQuery = true)
    int softDeleteByJobScheduleId(@Param("scheduleId") Long scheduleId);
}
