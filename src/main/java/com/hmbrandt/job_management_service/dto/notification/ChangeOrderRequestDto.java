package com.hmbrandt.job_management_service.dto.notification;

import com.hmbrandt.job_management_service.dto.create.ChangeOrderCreateDto;

public record ChangeOrderRequestDto(
        ChangeOrderCreateDto order,
        JobDataDto job
) {}
