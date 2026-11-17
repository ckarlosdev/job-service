package com.hmbrandt.job_management_service.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

public record PatchScheduleRequest (
        LocalDate date, // Opcional si solo se cambia la hora

        @JsonProperty("start_time")
        @Schema(type = "string", format = "time", example = "08:30:00")
        LocalTime startTime, // Opcional

        @JsonProperty("schedule_comment")
        String scheduleComment
){}
