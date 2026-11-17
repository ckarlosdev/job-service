package com.hmbrandt.job_management_service.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateEmployeesRequest(
        @NotNull(message = "La lista de employee_ids no puede ser nula")
        @JsonProperty("employee_ids")
        Set<Integer> employeeIds
) {
}
