package com.hmbrandt.job_management_service.exception;

import java.util.Objects;

public class UnprocessableEntityException extends RuntimeException{
    private final String code;

    public UnprocessableEntityException(String message) {
        super(message);
        this.code = "UNPROCESSABLE_ENTITY";
    }

    public UnprocessableEntityException(String code, String message) {
        super(message);
        this.code = Objects.requireNonNullElse(code, "UNPROCESSABLE_ENTITY");
    }

    public String getCode() {
        return code;
    }
}
