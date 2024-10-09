package com.test.template.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class TemplateException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public TemplateException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TemplateException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
