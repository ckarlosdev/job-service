package com.hmbrandt.job_management_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {
    @Value("${services.notification.url}")
    private String notificationServiceUrl;

    @Bean
    public RestClient notificationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());

        return RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
