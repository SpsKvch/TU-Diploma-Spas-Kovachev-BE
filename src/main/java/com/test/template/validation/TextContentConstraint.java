package com.test.template.validation;

import com.test.template.validation.annotations.ValidContent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class TextContentConstraint implements ConstraintValidator<ValidContent, String> {

  private String message;
  private int maxLength;

  @Override
  public boolean isValid(String content, ConstraintValidatorContext context) {

    context.disableDefaultConstraintViolation();
    if (StringUtils.isBlank(content)) {
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
      return false;
    }

    if (content.length() > maxLength) {
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
      return false;
    }

    return true;
  }

  @Override
  public void initialize(ValidContent constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
    message = constraintAnnotation.message();
    maxLength = constraintAnnotation.length();
  }

}
