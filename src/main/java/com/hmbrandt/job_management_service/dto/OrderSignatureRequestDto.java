package com.hmbrandt.job_management_service.dto;

public record OrderSignatureRequestDto(
        String signatureRole,
        String signatureData,
        String signatureName
) {}
