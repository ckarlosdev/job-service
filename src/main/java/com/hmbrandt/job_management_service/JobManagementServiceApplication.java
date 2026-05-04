package com.hmbrandt.job_management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JobManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobManagementServiceApplication.class, args);
	}

}
