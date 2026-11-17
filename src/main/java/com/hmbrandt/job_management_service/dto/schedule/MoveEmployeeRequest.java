package com.hmbrandt.job_management_service.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record MoveEmployeeRequest(
        @NotNull
        @JsonProperty("source_schedule_id")
        Long sourceScheduleId,

        @NotNull
        @JsonProperty("target_schedule_id")
        Long targetScheduleId,

        @NotNull
        @JsonProperty("employee_id")
        Integer employeeId
) {
}
