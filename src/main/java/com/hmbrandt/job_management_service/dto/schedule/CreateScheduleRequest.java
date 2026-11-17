package com.hmbrandt.job_management_service.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record CreateScheduleRequest(
        @NotNull(message = "El job_id es obligatorio")
        @JsonProperty("job_id")
        Integer jobId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotNull(message = "La hora de inicio es obligatoria")
        @JsonProperty("start_time")
        @Schema(type = "string", format = "time", example = "08:30:00")
        LocalTime startTime,

        @JsonProperty("schedule_comment")
        String scheduleComment,

        @JsonProperty("employee_ids")
        Set<Integer> employeeIds // Opcional
) {
}
