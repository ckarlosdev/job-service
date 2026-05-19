package com.hmbrandt.job_management_service.dto;

public record TaskToolResponseDto(
        Long id,
        String toolName,
        Integer quantity
) {}
