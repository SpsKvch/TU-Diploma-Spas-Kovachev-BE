package com.test.exceptions;

import com.test.security.exceptions.JwtAuthenticationException;
import com.test.template.exceptions.TemplateException;
import com.test.user.exception.TemplateUserException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final int STACK_TRACE_LENGTH = 20;

  @ExceptionHandler(TemplateException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleTemplateException(final TemplateException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(TemplateUserException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleTemplateUserException(final TemplateUserException e) {
    return new ResponseEntity<>(buildResponse(e), e.getStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
    return new ResponseEntity<>(buildResponse(e, getConstraintErrorMessages(e)), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleMethodArgumentNotValidException(final HttpMessageNotReadableException e) {
    return new ResponseEntity<>(buildResponse(e), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(JwtAuthenticationException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleUsernameNotFoundException(final JwtAuthenticationException e) {
    return new ResponseEntity<>(buildResponse(e), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ExceptionResponseWrapper> handleUsernameNotFoundException(final BadCredentialsException e) {
    return new ResponseEntity<>(buildResponse(e), HttpStatus.UNAUTHORIZED);
  }

  private List<String> getConstraintErrorMessages(final MethodArgumentNotValidException e) {
    List<String> errorMessages = new ArrayList<>();
    List<ObjectError> fieldErrors = e.getBindingResult().getAllErrors();

    if (!fieldErrors.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
      fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).forEach(errorMessages::add);
      return errorMessages;
    }
    return Collections.singletonList("Invalid input");
  }

  private ExceptionResponseWrapper buildResponse(final Exception e) {
    return buildResponse(e, Collections.singletonList(e.getMessage()));
  }

  private ExceptionResponseWrapper buildResponse(final Exception e, final List<String> messages) {
    int stackTraceLength = e.getStackTrace().length;
    int responseStackTraceSize = Math.min(stackTraceLength, STACK_TRACE_LENGTH);

    return ExceptionResponseWrapper.builder()
        .messages(messages)
        .exceptionName(e.getClass().getSimpleName())
        .stackTrace(Arrays.stream(e.getStackTrace())
            .map(StackTraceElement::toString)
            .toList().subList(0, responseStackTraceSize - 1))
        .build();
  }

}
