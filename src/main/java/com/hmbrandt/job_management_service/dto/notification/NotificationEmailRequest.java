package com.hmbrandt.job_management_service.dto.notification;

public record NotificationEmailRequest(
        String to,
        String subject,
        String htmlContent
) {
}
