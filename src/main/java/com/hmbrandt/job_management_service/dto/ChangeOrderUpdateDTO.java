package com.hmbrandt.job_management_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ChangeOrderUpdateDTO(
        Long employeeId,
        LocalDate orderDate,
        Integer orderNumber,
        BigDecimal amount,
        String orderStatus,
        String updatedBy,
        List<OrderTaskResponseDto>tasks,
        List<OrderSignatureResponseDto> signatures
) {}
