package com.hmbrandt.job_management_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderTaskResponseDto(
        Long id,
        String taskName,
        String taskDescription,
        Integer foreman,
        Integer labor,
        Integer other,
        BigDecimal totalHours,
        String comments,
        String equipmentComments,
        String toolComments,
        String dumpsterComments,
        List<TaskEquipmentResponseDto>  equipments,
        List<TaskToolResponseDto>  tools,
        List<TaskDumpsterResponseDto>  dumpsters
) {
}
