package com.test.template.service.helpers;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.tracked.UpdateTrackedTemplateRequest;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Component
public class ProgressionStatusHelper {

  private static final String ALL_STEPS_ABANDONED_ERROR_MESSAGE = "All steps cannot have ABANDONED status";
  private static final String STATUS_NOT_APPLICABLE_TO_STEP = "%s status cannot be applied to steps";
  private static final String STATUS_MISMATCH_ERROR_MESSAGE = "%s status cannot precede %s status";
  private static final String UNABLE_TO_PUT_COMPLETED_TEMPLATE_ON_HOLD = "Cannot put %s template on hold";
  private static final String UNABLE_TO_ABANDON_COMPLETED_TEMPLATE = "Cannot abandon completed template";
  private static final String UNABLE_TO_OVERRIDE_NOT_STARTED_STATUS = "Cannot change the status of a template with no progress";

  public void validateStatusApplicability(final Collection<TrackedStep> steps) {
    steps.forEach(step -> {
      if (step.getProgressionStatus().isNotStarted() && (step.getTimeSpent() != null && !step.getTimeSpent().isZero())) {
        throw new TemplateException("Unable to log time in step which has not been started", HttpStatus.BAD_REQUEST);
      }

      if (step.getProgressionStatus().isCompleted() && (step.getTimeSpent() == null || step.getTimeSpent().isZero())) {
        throw new TemplateException("Unable to mark step as completed with no time spent", HttpStatus.BAD_REQUEST);
      }
    });
  }

  public void validateProgressionStepOrdering(final List<TrackedStep> steps) {

    TrackedStep previousStep = null;
    for (TrackedStep step : steps) {
      ProgressionStatus currentStatus = step.getProgressionStatus();

      if (currentStatus.isNotApplicableToStep()) {
        throw new TemplateException(String.format(STATUS_NOT_APPLICABLE_TO_STEP, currentStatus), HttpStatus.BAD_REQUEST);
      }

      //Skipped on first iteration or if previous status is optional
      if (Objects.nonNull(previousStep)) {
        ProgressionStatus previousStatus = previousStep.getProgressionStatus();
        switch (currentStatus) {
          case IN_PROGRESS, COMPLETED -> validateStepStatusOrdering(previousStatus::isEffectivelyCompleted,
              previousStep, currentStatus);
          case ABANDONED -> validateStepStatusOrdering(previousStatus::canAbandon,
              previousStep, currentStatus);
        }
      }
      if (!step.getOptional()) {
        previousStep = step;
      }
    }
  }

  public ProgressionStatus aggregateStepProgressionStatus(final List<TrackedStep> steps) {
    //Calculate based on steps that are not optional
    List<TrackedStep> onlyRequiredSteps = steps.stream().filter(step -> !step.getOptional()).toList();
    int requiredStepsCount = onlyRequiredSteps.size();
    Map<ProgressionStatus, Integer> statusCounts = new HashMap<>();
    onlyRequiredSteps.stream()
        .map(TrackedStep::getProgressionStatus)
        .forEach(status -> {
          statusCounts.putIfAbsent(status, 0);
          statusCounts.compute(status, (key, val) -> val += 1);
        });

    if (statusCounts.getOrDefault(ProgressionStatus.ABANDONED, 0) == requiredStepsCount) {
      throw new TemplateException(ALL_STEPS_ABANDONED_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    if (statusCounts.getOrDefault(ProgressionStatus.NOT_STARTED, 0) == requiredStepsCount) {
      return ProgressionStatus.NOT_STARTED;
    }

    if (statusCounts.getOrDefault(ProgressionStatus.COMPLETED, 0) == requiredStepsCount) {
      return ProgressionStatus.COMPLETED;
    }

    if (statusCounts.getOrDefault(ProgressionStatus.NOT_STARTED, 0) == 0 &&
        statusCounts.getOrDefault(ProgressionStatus.IN_PROGRESS, 0) == 0) {
      return ProgressionStatus.PARTIALLY_COMPLETED;
    }

    return ProgressionStatus.IN_PROGRESS;
  }

  /**
   * Called when a template level {@link ProgressionStatus} is passed through the {@link UpdateTrackedTemplateRequest}.
   * Method assumes it will only receive ON_HOLD or ABANDONED {@link ProgressionStatus}.
   *
   * @param requestStatus   The status passed in the request
   * @param aggregateStatus The status of the saved template
   */
  public void checkProgressionStatusApplicability(final ProgressionStatus requestStatus,
      final ProgressionStatus aggregateStatus) {
    if (aggregateStatus.isEffectivelyCompleted() && requestStatus.isOnHold()) {
      throw new TemplateException(String.format(UNABLE_TO_PUT_COMPLETED_TEMPLATE_ON_HOLD, aggregateStatus),
          HttpStatus.BAD_REQUEST);
    }

    if (aggregateStatus.isCompleted() && requestStatus.isAbandoned()) {
      throw new TemplateException(UNABLE_TO_ABANDON_COMPLETED_TEMPLATE, HttpStatus.BAD_REQUEST);
    }

    if (aggregateStatus.isNotStarted()) {
      throw new TemplateException(UNABLE_TO_OVERRIDE_NOT_STARTED_STATUS, HttpStatus.BAD_REQUEST);
    }

    log.info("Status {} is applicable as aggregate status for template is {}", requestStatus, aggregateStatus);
  }

  /**
   * @param fulfilledCondition Constraint previous step needs to fulfill
   */
  private void validateStepStatusOrdering(Supplier<Boolean> fulfilledCondition,
      final TrackedStep previousStep,
      final ProgressionStatus currentStatus) {
    //Throws exception if status-specific condition is not met and previous step is not optional
    if (!fulfilledCondition.get() && !previousStep.getOptional()) {
      throw new TemplateException(
          String.format(STATUS_MISMATCH_ERROR_MESSAGE, previousStep.getProgressionStatus(), currentStatus), HttpStatus.BAD_REQUEST);
    }
  }

}
