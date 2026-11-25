package com.hmbrandt.job_management_service.entity.schedule;

import com.hmbrandt.job_management_service.exception.UnprocessableEntityException;

public enum AbsenceType {
    Off,
    Vacation,
    Sick,
    Other;

    public static AbsenceType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new UnprocessableEntityException("absenceType es obligatorio.");
        }
        String normalized = value.trim();
        for (AbsenceType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new UnprocessableEntityException("absenceType no válido: " + value);
    }
}
