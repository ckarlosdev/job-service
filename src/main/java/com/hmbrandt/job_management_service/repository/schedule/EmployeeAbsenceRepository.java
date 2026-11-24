package com.hmbrandt.job_management_service.repository.schedule;

import com.hmbrandt.job_management_service.entity.schedule.EmployeeAbsence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAbsenceRepository extends JpaRepository<EmployeeAbsence, Long> {
    List<EmployeeAbsence> findByDateBetweenOrderByDateAscIdAsc(LocalDate from, LocalDate to);

    boolean existsByEmployeesIdAndDate(Integer employeesId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM EmployeeAbsence a
        WHERE a.employeesId = :employeesId AND a.date = :date
        """)
    Optional<EmployeeAbsence> findByEmployeeIdAndDateForUpdate(
            @Param("employeesId") Integer employeesId,
            @Param("date") LocalDate date
    );
}
