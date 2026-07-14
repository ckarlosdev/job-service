package com.hmbrandt.job_management_service.dto;

public record OrderSignatureResponseDto(
        Long id,
        String signatureRole,
        String filePath,
        String signatureName
) {}
