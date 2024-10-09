package com.test.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class TemplateUserException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public TemplateUserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TemplateUserException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
