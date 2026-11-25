package com.hmbrandt.job_management_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderNumberRequest(
        @NotNull(message = "orderNumber is required")
        @Min(value = 1, message = "orderNumber must be greater than or equal to 1")
        Integer orderNumber
) {
}
