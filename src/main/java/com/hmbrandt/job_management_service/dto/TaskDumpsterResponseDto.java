package com.hmbrandt.job_management_service.dto;

public record TaskDumpsterResponseDto(
        Long id,
        String materialType,
        String dumpsterSize,
        Integer quantity
) {}
