package com.test.template.models.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateStep {

    private String title;
    private String content;
    private String imageUrl;
    private Duration minTimeEstimate;
    private Duration maxTimeEstimate;
    private Boolean important;
    private Boolean optional;

    public void setTitle(String title) {
        if(!StringUtils.isBlank(title)) {
            this.title = title;
        }
    }

    public void setContent(String content) {
        if(!StringUtils.isBlank(content)) {
            this.content = content;
        }
    }

    public void setMinAndMaxTimeEstimateFromMedian(Duration timeSpent) {
        if (timeSpent.compareTo(maxTimeEstimate) > 0) {
            Duration difference = timeSpent.minus(maxTimeEstimate);
            minTimeEstimate = maxTimeEstimate;
            maxTimeEstimate = timeSpent.plus(difference);
        } else if (timeSpent.compareTo(minTimeEstimate) < 0) {
            maxTimeEstimate = minTimeEstimate;
            minTimeEstimate = timeSpent;
        }
    }
}
