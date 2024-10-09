package com.test.template.models.tracked;

import com.test.template.models.Requirement;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TrackedStepRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

import static com.test.template.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrackedTemplateRequest {

    @Size(max = MAX_TEMPLATE_TITLE_LENGTH, message = TEMPLATE_TITLE_ERROR_MESSAGE)
    private String newTitle;
    @Size(max = MAX_TEMPLATE_CONTENT_LENGTH, message = TEMPLATE_CONTENT_ERROR_MESSAGE)
    private String markedUpContent;
    @Valid
    private Map<Integer, TrackedStepRequest> trackedSteps;
    private Set<Requirement> trackedRequirements;
    private ProgressionStatus newStatus;

}
