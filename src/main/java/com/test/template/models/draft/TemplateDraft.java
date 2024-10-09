package com.test.template.models.draft;


import com.test.template.models.Requirement;
import com.test.template.models.steps.TemplateStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDraft {

    @Id
    private String id;
    private String creatorName;
    private String parentTemplateId;
    private String title;
    private String content;
    private String imageUrl;
    private Set<Requirement> requirements;
    private List<TemplateStep> steps;
    private LocalDateTime creationTime;
    private LocalDateTime lastUpdateTime;
}
