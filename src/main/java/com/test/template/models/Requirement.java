package com.test.template.models;

import com.test.template.validation.annotations.ValidContent;
import jakarta.validation.constraints.Size;
import lombok.*;

import static com.test.template.validation.ValidationConstants.MAX_REQUIREMENT_LENGTH;
import static com.test.template.validation.ValidationConstants.REQUIREMENT_CONTENT_ERROR_MESSAGE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Requirement {

    @ValidContent(length = MAX_REQUIREMENT_LENGTH, message = REQUIREMENT_CONTENT_ERROR_MESSAGE)
    private String content;
    @Size(max = 50, message = "Length of requirement group name cannot exceed 50 characters")
    private String group;
    private boolean optional;
    private String imageUrl;

}
