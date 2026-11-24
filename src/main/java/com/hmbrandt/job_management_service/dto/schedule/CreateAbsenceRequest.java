package com.hmbrandt.job_management_service.dto.schedule;

public record CreateAbsenceRequest(
        Integer employeesId,
        String date,
        String absenceType,
        String absenceComment
) {
}
