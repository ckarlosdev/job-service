package com.hmbrandt.job_management_service.dto.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdatedScheduleResponse(
        Long id,
        Integer jobId,
        LocalDate scheduledDate,
        LocalTime startTime,
        String scheduleComment,
        List<Integer> employeeIds
) {
}
