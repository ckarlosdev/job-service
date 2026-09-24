package com.hmbrandt.job_management_service.client;

import com.hmbrandt.job_management_service.dto.notification.NotificationEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClient {
    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private final RestClient notificationRestClient;

    public NotificationClient(RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    public void sendEmailNotification(String to, String subject, String htmlContent) {
        try {
            NotificationEmailRequest request = new NotificationEmailRequest(to, subject, htmlContent);

            notificationRestClient.post()
                    .uri("/api/v2/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notification request successfully sent for: {}", to);

        } catch (Exception e) {
            // Se captura la excepción para evitar que un fallo en la notificación rompa la transacción principal
            log.error("Error communicating with the notification-service for {}: {}", to, e.getMessage());
        }
    }
}
