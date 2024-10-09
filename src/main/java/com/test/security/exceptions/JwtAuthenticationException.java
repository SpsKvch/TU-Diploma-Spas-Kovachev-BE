package com.test.security.exceptions;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationException extends RuntimeException {


  public JwtAuthenticationException(String message) {
    super(message);
  }

  public JwtAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
