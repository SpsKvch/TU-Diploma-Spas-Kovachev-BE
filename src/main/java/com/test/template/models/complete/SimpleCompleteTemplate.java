package com.test.template.models.complete;

import com.test.template.models.Requirement;
import com.test.template.models.steps.TemplateStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleCompleteTemplate {

    private String id;
    private String creatorName;
    private String title;
    private String imageUrl;
    private Set<Requirement> requirements;
    private String content;
    private List<TemplateStep> steps;
    private String parentId;
}
