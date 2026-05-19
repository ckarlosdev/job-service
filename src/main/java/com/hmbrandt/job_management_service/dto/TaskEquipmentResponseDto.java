package com.hmbrandt.job_management_service.dto;

public record TaskEquipmentResponseDto(
        Long id,
        String equipmentName,
        Integer quantity
) {}
