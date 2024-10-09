package com.test.template.models.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateStepRequest {

    private String title;
    private String content;
    private String imageUrl;
    private Duration minTimeEstimate;
    private Duration maxTimeEstimate;
    private boolean important;
    private boolean optional;
}
