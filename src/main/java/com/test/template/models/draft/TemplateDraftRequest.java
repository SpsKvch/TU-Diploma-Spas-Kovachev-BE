package com.test.template.models.draft;

import com.test.template.models.Requirement;
import com.test.template.models.steps.TemplateStepRequest;
import com.test.template.validation.annotations.ValidContent;
import com.test.template.validation.annotations.ValidSteps;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

import static com.test.template.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDraftRequest {

    @ValidContent(message = TEMPLATE_TITLE_ERROR_MESSAGE, length = MAX_TEMPLATE_TITLE_LENGTH)
    private String title;
    @ValidContent(message = STEP_CONTENT_ERROR_MESSAGE, length = MAX_TEMPLATE_CONTENT_LENGTH)
    private String content;
    private String imageUrl;
    @ValidSteps
    @NotNull(message = NULL_STEPS_ERROR_MESSAGE)
    private List<TemplateStepRequest> steps;
    @Valid
    private Set<Requirement> requirements;
}
