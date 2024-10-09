package com.test.template.models.steps;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.test.template.models.enums.ProgressionStatus;
import lombok.*;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedStep {

  private String markedUpTitle;
  private String markedUpContent;
  private String markedUpImage;
  private List<String> notes;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Duration timeSpent;
  private ProgressionStatus progressionStatus;
  private Boolean optional;

  public void setMarkedUpTitle(String newTitle) {
    if (Objects.nonNull(newTitle)) {
      this.markedUpTitle = newTitle;
    }
  }

  public void setMarkedUpContent(String newContent) {
    if (Objects.nonNull(newContent)) {
      this.markedUpContent = newContent;
    }
  }

}
