package com.test.services.helpers;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TrackedStep;
import com.test.template.service.helpers.ProgressionStatusHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static com.test.template.models.enums.ProgressionStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@Disabled
@ExtendWith(SpringExtension.class)
public class ProgressionStatusHelperTest {

    private static final String STATUS_ORDER_ERROR = "%s status cannot precede %s status";
    private static final String STATUS_NOT_APPLICABLE = "%s status cannot be applied to steps";
    private static final String ALL_ABANDONED = "All steps cannot have ABANDONED status";
    private static final String UNABLE_TO_PUT_COMPLETED_TEMPLATE_ON_HOLD = "Cannot put %s template on hold";
    private static final String UNABLE_TO_ABANDON_COMPLETED_TEMPLATE = "Cannot abandon completed template";
    private static final String UNABLE_TO_OVERRIDE_NOT_STARTED_STATUS = "Cannot change the status of a template with no progress";

    private final ProgressionStatusHelper progressionStatusHelper = new ProgressionStatusHelper();

    @Test
    public void validateStatusSuccess() {
        expectValidProgressionStatus(new ProgressionStatus[]{NOT_STARTED, NOT_STARTED, NOT_STARTED});
        expectValidProgressionStatus(new ProgressionStatus[]{IN_PROGRESS, NOT_STARTED, NOT_STARTED});
        expectValidProgressionStatus(new ProgressionStatus[]{IN_PROGRESS, ABANDONED, IN_PROGRESS});
        expectValidProgressionStatus(new ProgressionStatus[]{COMPLETED, NOT_STARTED, NOT_STARTED});
        expectValidProgressionStatus(new ProgressionStatus[]{COMPLETED, ABANDONED, IN_PROGRESS});
        expectValidProgressionStatus(new ProgressionStatus[]{ABANDONED, NOT_STARTED, NOT_STARTED});
        expectValidProgressionStatus(new ProgressionStatus[]{ABANDONED, IN_PROGRESS, NOT_STARTED});
        expectValidProgressionStatus(new ProgressionStatus[]{ABANDONED, ABANDONED, NOT_STARTED});

        //Optional steps present
        expectValidProgressionStatusWithOptional(new ProgressionStatus[]{COMPLETED, NOT_STARTED, IN_PROGRESS});
        expectValidProgressionStatusWithOptional(new ProgressionStatus[]{COMPLETED, NOT_STARTED, COMPLETED});
        expectValidProgressionStatusWithOptional(new ProgressionStatus[]{ABANDONED, NOT_STARTED, COMPLETED});
        expectValidProgressionStatusWithOptional(new ProgressionStatus[]{ABANDONED, COMPLETED, IN_PROGRESS});
    }

    @Test
    public void validateStatusException() {
        String message = String.format(STATUS_ORDER_ERROR, NOT_STARTED, IN_PROGRESS);
        expectProgressionStatusException(new ProgressionStatus[]{NOT_STARTED, IN_PROGRESS, COMPLETED}, message);

        message = String.format(STATUS_ORDER_ERROR, NOT_STARTED, ABANDONED);
        expectProgressionStatusException(new ProgressionStatus[]{NOT_STARTED, NOT_STARTED, ABANDONED}, message);

        message = String.format(STATUS_ORDER_ERROR, IN_PROGRESS, IN_PROGRESS);
        expectProgressionStatusException(new ProgressionStatus[]{IN_PROGRESS, IN_PROGRESS, IN_PROGRESS}, message);

        message = String.format(STATUS_ORDER_ERROR, IN_PROGRESS, COMPLETED);
        expectProgressionStatusException(new ProgressionStatus[]{COMPLETED, IN_PROGRESS, COMPLETED}, message);

        message = String.format(STATUS_ORDER_ERROR, NOT_STARTED, ABANDONED);
        expectProgressionStatusException(new ProgressionStatus[]{COMPLETED, NOT_STARTED, ABANDONED}, message);

        message = String.format(STATUS_ORDER_ERROR, NOT_STARTED, IN_PROGRESS);
        expectProgressionStatusException(new ProgressionStatus[]{ABANDONED, NOT_STARTED, IN_PROGRESS}, message);

        message = String.format(STATUS_NOT_APPLICABLE, ON_HOLD);
        expectProgressionStatusException(new ProgressionStatus[]{ON_HOLD, NOT_STARTED, NOT_STARTED}, message);

        message = String.format(STATUS_NOT_APPLICABLE, PARTIALLY_COMPLETED);
        expectProgressionStatusException(new ProgressionStatus[]{PARTIALLY_COMPLETED, NOT_STARTED, NOT_STARTED}, message);

        //Optional steps present
        message = String.format(STATUS_ORDER_ERROR, IN_PROGRESS, COMPLETED);
        expectProgressionStatusExceptionWithOptional(new ProgressionStatus[]{IN_PROGRESS, COMPLETED, NOT_STARTED}, message);
        message = String.format(STATUS_ORDER_ERROR, NOT_STARTED, IN_PROGRESS);
        expectProgressionStatusExceptionWithOptional(new ProgressionStatus[]{NOT_STARTED, IN_PROGRESS, IN_PROGRESS}, message);
        message = String.format(STATUS_ORDER_ERROR, NOT_STARTED, ABANDONED);
        expectProgressionStatusExceptionWithOptional(new ProgressionStatus[]{NOT_STARTED, ABANDONED, NOT_STARTED}, message);
        message = String.format(STATUS_ORDER_ERROR, IN_PROGRESS, COMPLETED);
        expectProgressionStatusExceptionWithOptional(new ProgressionStatus[]{IN_PROGRESS, ABANDONED, COMPLETED}, message);
    }

    @Test
    public void aggregateStatusSuccess() {
        expectAggregateStatus(new ProgressionStatus[]{NOT_STARTED, NOT_STARTED, NOT_STARTED}, NOT_STARTED);
        expectAggregateStatus(new ProgressionStatus[]{ABANDONED, NOT_STARTED, NOT_STARTED}, IN_PROGRESS);
        expectAggregateStatus(new ProgressionStatus[]{IN_PROGRESS, NOT_STARTED, NOT_STARTED}, IN_PROGRESS);
        expectAggregateStatus(new ProgressionStatus[]{COMPLETED, IN_PROGRESS, NOT_STARTED}, IN_PROGRESS);
        expectAggregateStatus(new ProgressionStatus[]{COMPLETED, NOT_STARTED, NOT_STARTED}, IN_PROGRESS);
        expectAggregateStatus(new ProgressionStatus[]{COMPLETED, ABANDONED, COMPLETED}, PARTIALLY_COMPLETED);
        expectAggregateStatus(new ProgressionStatus[]{COMPLETED, COMPLETED, COMPLETED}, COMPLETED);
    }

    @Test
    public void aggregateStatusException() {
        List<TrackedStep> steps = createStatusesList(new ProgressionStatus[]{ABANDONED, ABANDONED, ABANDONED});
        TemplateException caught = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.aggregateStepProgressionStatus(steps));
        assertEquals(ALL_ABANDONED, caught.getMessage());
    }

    @Test
    public void progressionStatusApplicabilityException() {
        String message = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.checkProgressionStatusApplicability(ON_HOLD, ABANDONED)).getMessage();
        assertEquals(String.format(UNABLE_TO_PUT_COMPLETED_TEMPLATE_ON_HOLD, ABANDONED), message);
        message = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.checkProgressionStatusApplicability(ON_HOLD, PARTIALLY_COMPLETED)).getMessage();
        assertEquals(String.format(UNABLE_TO_PUT_COMPLETED_TEMPLATE_ON_HOLD, PARTIALLY_COMPLETED), message);
        message = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.checkProgressionStatusApplicability(ON_HOLD, COMPLETED)).getMessage();
        assertEquals(String.format(UNABLE_TO_PUT_COMPLETED_TEMPLATE_ON_HOLD, COMPLETED), message);
        message = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.checkProgressionStatusApplicability(ABANDONED, COMPLETED)).getMessage();
        assertEquals(UNABLE_TO_ABANDON_COMPLETED_TEMPLATE, message);
        message = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.checkProgressionStatusApplicability(ON_HOLD, NOT_STARTED)).getMessage();
        assertEquals(UNABLE_TO_OVERRIDE_NOT_STARTED_STATUS, message);
        message = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.checkProgressionStatusApplicability(ABANDONED, NOT_STARTED)).getMessage();
        assertEquals(UNABLE_TO_OVERRIDE_NOT_STARTED_STATUS, message);
    }

    private void expectValidProgressionStatus(ProgressionStatus[] statuses) {
        List<TrackedStep> steps = createStatusesList(statuses);
        progressionStatusHelper.validateProgressionStepOrdering(steps);
    }

    private void expectValidProgressionStatusWithOptional(ProgressionStatus[] statuses) {
        List<TrackedStep> steps = createOptionalStatusesList(statuses);
        progressionStatusHelper.validateProgressionStepOrdering(steps);
    }

    private void expectProgressionStatusException(ProgressionStatus[] statuses, String message) {
        List<TrackedStep> steps = createStatusesList(statuses);
        TemplateException caught = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.validateProgressionStepOrdering(steps));
        assertEquals(message, caught.getMessage());
    }

    private void expectProgressionStatusExceptionWithOptional(ProgressionStatus[] statuses, String message) {
        List<TrackedStep> steps = createOptionalStatusesList(statuses);
        TemplateException caught = assertThrows(TemplateException.class,
                () -> progressionStatusHelper.validateProgressionStepOrdering(steps));
        assertEquals(message, caught.getMessage());
    }

    private void expectAggregateStatus(ProgressionStatus[] statuses, ProgressionStatus expected) {
        List<TrackedStep> steps = createStatusesList(statuses);
        ProgressionStatus result = progressionStatusHelper.aggregateStepProgressionStatus(steps);
        assertEquals(expected, result);
    }

    private List<TrackedStep> createStatusesList(ProgressionStatus[] statuses) {
        List<TrackedStep> steps = new ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            steps.add(i, buildStep(statuses[i], false));
        }
        return steps;
    }

    private List<TrackedStep> createOptionalStatusesList(ProgressionStatus[] statuses) {
        List<TrackedStep> steps = new ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            steps.add(i, buildStep(statuses[i], i%2 == 1));
        }
        return steps;
    }

    private TrackedStep buildStep(ProgressionStatus status, boolean optional) {
        return TrackedStep.builder().progressionStatus(status).optional(optional).build();
    }

}
