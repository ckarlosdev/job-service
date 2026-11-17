package com.hmbrandt.job_management_service.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record CreateScheduleRequest(
        @NotNull(message = "El job_id es obligatorio")
        @JsonProperty("jobsId")
        Integer jobId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotNull(message = "La hora de inicio es obligatoria")
        @JsonProperty("startTime")
        @Schema(type = "string", format = "time", example = "08:30:00")
        LocalTime startTime,

        @JsonProperty("scheduleComment")
        String scheduleComment,

        @JsonProperty("employeeIds")
        Set<Integer> employeeIds // Opcional
) {
}
