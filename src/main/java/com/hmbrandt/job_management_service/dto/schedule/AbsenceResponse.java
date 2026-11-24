package com.hmbrandt.job_management_service.dto.schedule;

import com.hmbrandt.job_management_service.entity.schedule.EmployeeAbsence;

import java.time.LocalDate;

public record AbsenceResponse(
        Long id,
        Integer employeesId,
        LocalDate date,
        String absenceType,
        String absenceComment
) {
    public static AbsenceResponse from(EmployeeAbsence entity) {
        return new AbsenceResponse(
                entity.getId(),
                entity.getEmployeesId(),
                entity.getDate(),
                entity.getAbsenceType().name(),
                entity.getAbsenceComment()
        );
    }
}
