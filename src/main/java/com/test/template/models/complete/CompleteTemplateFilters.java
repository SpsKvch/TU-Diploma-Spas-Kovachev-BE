package com.test.template.models.complete;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTemplateFilters {

  private String title;
  private String categoryName;
  private Set<String> tags;
  private LocalDate minDate;
  private LocalDate maxDate;
  private Boolean isOriginal;
  private Integer minApprovalPercent;
  private Duration maxCompletionTime;
  private Integer minCompletionRate;

}
