package com.hmbrandt.job_management_service.dto.create;

import com.hmbrandt.job_management_service.dto.OrderSignatureRequestDto;
import com.hmbrandt.job_management_service.dto.OrderSignatureResponseDto;
import com.hmbrandt.job_management_service.dto.OrderTaskResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ChangeOrderCreateDto(
        Long id,
        Long jobId,
        Long employeeId,
        LocalDate orderDate,
        Integer orderNumber,
        BigDecimal amount,
        String orderStatus,
        List<OrderTaskResponseDto> tasks,
        List<OrderSignatureRequestDto> signatures
) {}
