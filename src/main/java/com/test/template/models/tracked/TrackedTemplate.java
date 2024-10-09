package com.test.template.models.tracked;

import com.test.template.models.Requirement;
import com.test.template.models.complete.SimpleCompleteTemplate;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TrackedStep;
import java.util.ArrayList;
import java.util.HashSet;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;


import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(unique = true, def = "ownerName: 1, originalTemplate.id: 1", name = "journal_index")
public class TrackedTemplate {

  @Id
  private String id;
  private String ownerName;
  private SimpleCompleteTemplate originalTemplate;
  private String newTitle;
  private String markedUpContent;
  private String markedUpImage;
  private List<TrackedStep> trackedSteps = new ArrayList<>();
  private Set<Requirement> trackedRequirements = new HashSet<>();
  private ProgressionStatus currentStatus;
  @DateTimeFormat(iso = DATE_TIME)
  private LocalDateTime creationTime;
  @DateTimeFormat(iso = DATE_TIME)
  private LocalDateTime updateTime;

}
