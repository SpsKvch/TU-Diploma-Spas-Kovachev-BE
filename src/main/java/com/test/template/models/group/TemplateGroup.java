package com.test.template.models.group;

import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.enums.AccessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateGroup {

    @Id
    private String id;
    private List<CompleteTemplate> templates;
    private String owner;
    private AccessStatus accessStatus;

}
