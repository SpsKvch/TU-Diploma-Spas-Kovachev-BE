package com.test.template.models.complete;

import com.test.template.models.ParentDetails;
import com.test.template.models.Requirement;
import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import com.test.template.models.steps.TemplateStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTemplate {

  @Id
  private String id;
  private int views;
  private int branches;
  private int totalJournals;
  private int completedJournals;
  private int approvals;
  private int totalEngagements;
  private String creatorName;
  private Set<String> sharedWith;
  private String title;
  private Set<Requirement> requirements;
  private String content;
  private String summary;
  private String imageUrl;
  private String categoryId;
  private String category;
  private Set<String> tags;
  private String associatedGroup;
  private AccessStatus accessStatus;
  private BranchPermission branchPermission;
  private List<TemplateStep> steps;
  private ParentDetails parentDetails;
  private LocalDateTime createTime;

  public int getCompletionRate() {
    return (int) Math.ceil((double) completedJournals / totalJournals * 100);
  }

  public int getTemplateApprovalPercent() {
    return (int) Math.ceil((double) approvals / totalEngagements * 100);
  }

  public Duration getTemplateMinDuration() {
    return steps.stream().map(TemplateStep::getMinTimeEstimate).reduce(Duration.ZERO, Duration::plus);
  }

  public Duration getTemplateMaxDuration() {
    return steps.stream().map(TemplateStep::getMaxTimeEstimate).reduce(Duration.ZERO, Duration::plus);
  }

  public Duration getTemplateMedianDuration() {
    return Duration.ZERO.plus(getTemplateMinDuration()).plus(getTemplateMaxDuration()).dividedBy(2);
  }

}
