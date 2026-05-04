package com.hmbrandt.job_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChangeOrderUpdateDTO(
       LocalDate orderDate,
       Integer orderNumber,
       String changeDescription,
       @Schema(description = "Nueva descripción del cambio", example = "Aumento de dineros")
       BigDecimal amount,
       String updatedBy,
       LocalDateTime updatedAt) {
}
