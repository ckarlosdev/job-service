package com.hmbrandt.job_management_service.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public record ScheduleResponse(
        Long id,
        @JsonProperty("job_id") Integer jobId,
        LocalDate date,
        @JsonProperty("start_time")
        @Schema(type = "string", format = "time", example = "08:30:00")
        LocalTime startTime,
        @JsonProperty("schedule_comment") String scheduleComment,
        @JsonProperty("employee_ids") Set<Integer> employeeIds,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
