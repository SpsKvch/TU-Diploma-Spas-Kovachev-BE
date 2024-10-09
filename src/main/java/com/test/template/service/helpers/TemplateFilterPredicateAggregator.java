package com.test.template.service.helpers;

import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CompleteTemplateFilters;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

public class TemplateFilterPredicateAggregator {

  private final PostQueryFilters filters;
  private final List<Predicate<CompleteTemplate>> predicates = new ArrayList<>();

  public TemplateFilterPredicateAggregator(CompleteTemplateFilters filters) {
    this.filters = new PostQueryFilters(filters);

    if (Objects.nonNull(filters.getTags()) && !filters.getTags().isEmpty()) {
      predicates.add(this::tagsFilter);
    }

    if (filters.getMinApprovalPercent() > 0) {
      predicates.add(this::approvalRatingsFilters);
    }

    if (filters.getMaxCompletionTime() != null &&
        !filters.getMaxCompletionTime().isZero()) {
      predicates.add(this::completionTimeFilter);
    }

    if (filters.getMinCompletionRate() > 0) {
      predicates.add(this::completionRateFilter);
    }
  }

  public boolean isEmpty() {
    return predicates.isEmpty();
  }

  public boolean execute(CompleteTemplate template) {
    for (Predicate<CompleteTemplate> predicate : predicates) {
      if (!predicate.test(template)) {
        return false;
      }
    }
    return true;
    //return predicates.parallelStream().allMatch(p -> p.test(template));
  }

  private boolean tagsFilter(CompleteTemplate template) {
    return !Collections.disjoint(template.getTags(), filters.getTags());
  }

  private boolean approvalRatingsFilters(CompleteTemplate template) {
    return template.getTotalEngagements() > 0
        && template.getTemplateApprovalPercent() >= filters.getMinApprovalPercent();
  }

  private boolean completionTimeFilter(CompleteTemplate template) {
    return !filters.getMaxCompletionTime().minus(template.getTemplateMedianDuration()).isNegative();
  }

  private boolean completionRateFilter(CompleteTemplate template) {
    return template.getCompletionRate() > filters.getMinCompletionRate();
  }

  @Getter
  @Setter
  private static class PostQueryFilters {
    private Set<String> tags;
    private int minApprovalPercent;
    private Duration maxCompletionTime;
    private int minCompletionRate;

    public PostQueryFilters(CompleteTemplateFilters filters) {
      this.tags = filters.getTags();
      this.minApprovalPercent = filters.getMinApprovalPercent();
      this.maxCompletionTime = filters.getMaxCompletionTime();
      this.minCompletionRate = filters.getMinCompletionRate();
    }
  }

}
