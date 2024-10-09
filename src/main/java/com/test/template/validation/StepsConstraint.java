package com.test.template.validation;

import com.test.template.models.steps.TemplateStepRequest;
import com.test.template.validation.annotations.ValidSteps;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;


import static com.test.template.validation.ValidationConstants.*;

@Slf4j
public class StepsConstraint implements ConstraintValidator<ValidSteps, List<TemplateStepRequest>> {

  private static final String TITLE_CONSTRAINT_NAME = "Title";
  private static final String CONTENT_CONSTRAINT_NAME = "Content";
  private static final String TIME_ESTIMATE_MISMATCH = "Maximum time estimate for step %s must be greater than the minimum";

  private static int currentStep;

  @Override
  public boolean isValid(List<TemplateStepRequest> steps, ConstraintValidatorContext context) {
    currentStep = 1;
    context.disableDefaultConstraintViolation();

    if (Objects.isNull(steps)) {
      log.info("Steps validation skipped as null value was provided");
      return true;
    }

    if (steps.isEmpty()) {
      context.buildConstraintViolationWithTemplate(NO_STEPS_ERROR_MESSAGE).addConstraintViolation();
      return false;
    }

    if (steps.size() < MIN_STEP_COUNT) {
      context.buildConstraintViolationWithTemplate(
          String.format(STEP_MIN_LENGTH_ERROR_MESSAGE, MIN_STEP_COUNT)).addConstraintViolation();
      return false;
    }

    if (steps.size() > MAX_STEPS_COUNT) {
      context.buildConstraintViolationWithTemplate(
          String.format(STEP_MAX_LENGTH_ERROR_MESSAGE, MAX_STEPS_COUNT)).addConstraintViolation();
      return false;
    }

    Set<String> errorMessages = new HashSet<>();

    steps.forEach(step -> errorMessages.addAll(getConstraintViolations(step)));
    if (!errorMessages.isEmpty()) {
      errorMessages.forEach(error -> context.buildConstraintViolationWithTemplate(error).addConstraintViolation());
      return false;
    }

    return true;
  }

  private Set<String> getConstraintViolations(TemplateStepRequest step) {
    Set<String> stepViolations = new HashSet<>();
    hasTextViolation(step.getTitle(), TITLE_CONSTRAINT_NAME, MAX_STEP_TITLE_LENGTH).ifPresent(stepViolations::add);
    hasTextViolation(step.getContent(), CONTENT_CONSTRAINT_NAME, MAX_STEP_CONTENT_LENGTH).ifPresent(stepViolations::add);
    Duration timeEstimateDifference = step.getMaxTimeEstimate().minus(step.getMinTimeEstimate());
    if (timeEstimateDifference.isNegative() || timeEstimateDifference.isZero()) {
      stepViolations.add(String.format(TIME_ESTIMATE_MISMATCH, currentStep));
    }
    currentStep++;
    return stepViolations;
  }

  private Optional<String> hasTextViolation(String content, String type, int length) {
    String message = null;
    if (StringUtils.isBlank(content)) {
      message = String.format(STEP_BLANK_ERROR_MESSAGE, type, currentStep);
    } else if (content.length() > length) {
      message = String.format(STEP_LENGTH_ERROR_MESSAGE, type, currentStep, MAX_STEP_CONTENT_LENGTH);
    }
    return Optional.ofNullable(message);
  }
}
