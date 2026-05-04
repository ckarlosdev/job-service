package com.hmbrandt.job_management_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChangeOrderResponseDTO(
        Long id,
        Long jobId,
        Long employeeId,
        LocalDate orderDate,
        Integer orderNumber,
        LocalDate contractDate,
        String changeDescription,
        BigDecimal amount,
        String createdBy,
        LocalDateTime createdAt
) {}
