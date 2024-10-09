package com.test.template.models.draft;

import com.test.template.models.Requirement;
import com.test.template.models.steps.TemplateStepRequest;
import com.test.template.validation.annotations.ValidSteps;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
public class PutTemplateDraftRequest {

    @Size(max = MAX_TEMPLATE_TITLE_LENGTH, message = TEMPLATE_TITLE_ERROR_MESSAGE)
    private String title;
    @Size(max = MAX_TEMPLATE_CONTENT_LENGTH, message = TEMPLATE_CONTENT_ERROR_MESSAGE)
    private String content;
    private String imageUrl;
    @ValidSteps
    private List<TemplateStepRequest> steps;
    @Valid
    private Set<Requirement> requirements;

}
