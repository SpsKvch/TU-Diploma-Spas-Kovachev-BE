package com.test.template.service.builders;

import com.test.template.mappers.TemplateMapper;
import com.test.template.mappers.TemplateStepMapper;
import com.test.template.models.ParentDetails;
import com.test.template.models.categorization.Category;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CreateCompleteTemplateRequest;
import com.test.template.models.complete.SimpleCompleteTemplate;
import com.test.template.models.draft.TemplateDraftRequest;
import com.test.template.models.draft.PutTemplateDraftRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TemplateStep;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.tracked.TrackedTemplate;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@AllArgsConstructor
public class TemplateBuilder {

  private final TemplateStepMapper stepMapper;
  private final TemplateMapper templateMapper;

  public TemplateDraft buildTemplateDraft(final TemplateDraftRequest request,
      final String creatorName) {
    return TemplateDraft.builder()
        .creatorName(creatorName)
        .title(request.getTitle())
        .content(request.getContent())
        .imageUrl(request.getImageUrl())
        .requirements(request.getRequirements())
        .steps(request.getSteps().stream().map(stepMapper::toStep).toList())
        .creationTime(LocalDateTime.now())
        .build();
  }

  public TemplateDraft buildDraftFromParent(final CompleteTemplate parentTemplate, final String creatorName) {
    return TemplateDraft.builder().creatorName(creatorName)
        .title(parentTemplate.getTitle())
        .content(parentTemplate.getContent())
        .imageUrl(parentTemplate.getImageUrl())
        .parentTemplateId(parentTemplate.getId())
        .requirements(parentTemplate.getRequirements())
        .steps(parentTemplate.getSteps())
        .creationTime(LocalDateTime.now())
        .build();
  }

  public TemplateDraft buildDraftFromJournal(final String creatorName, final TrackedTemplate journal,
      final boolean ignoreAbandoned) {
    SimpleCompleteTemplate original = journal.getOriginalTemplate();
    return TemplateDraft.builder()
        .creatorName(creatorName)
        .parentTemplateId(original.getId())
        .title(StringUtils.isBlank(journal.getNewTitle()) ? original.getTitle() : journal.getNewTitle())
        .content(StringUtils.isBlank(journal.getMarkedUpContent()) ? original.getContent() : journal.getMarkedUpContent())
        .imageUrl(StringUtils.isBlank(journal.getMarkedUpImage()) ? original.getImageUrl() : journal.getMarkedUpImage())
        .requirements(journal.getTrackedRequirements())
        .steps(buildDraftStepsFromJournal(journal.getTrackedSteps(), original.getSteps(), ignoreAbandoned))
        .creationTime(LocalDateTime.now()).build();
  }

  private List<TemplateStep> buildDraftStepsFromJournal(final List<TrackedStep> journalSteps,
      final List<TemplateStep> completedSteps, final boolean ignoreAbandoned) {
    List<TemplateStep> aggregateList = new ArrayList<>(completedSteps.size());
    Iterator<TemplateStep> completeStepIterator = completedSteps.iterator();
    if (completeStepIterator.hasNext()) {
      journalSteps.forEach(step -> {
        if (ignoreAbandoned && step.getProgressionStatus().isAbandoned()) {
          return;
        }
        TemplateStep stepToAdd = completeStepIterator.next();
        stepToAdd.setTitle(step.getMarkedUpTitle());
        stepToAdd.setContent(step.getMarkedUpContent());
        stepToAdd.setImageUrl(step.getMarkedUpImage());
        stepToAdd.setMinAndMaxTimeEstimateFromMedian(step.getTimeSpent());

        aggregateList.add(stepToAdd);
      });
    }
    return aggregateList;
  }

  public CompleteTemplate buildTemplateFromDraft(final CreateCompleteTemplateRequest request, final TemplateDraft draft,
      final Category category, final Set<String> tags) {
    return CompleteTemplate.builder()
        .creatorName(draft.getCreatorName())
        .sharedWith(request.getSharedWith())
        .title(draft.getTitle())
        .requirements(draft.getRequirements())
        .content(draft.getContent())
        .imageUrl(draft.getImageUrl())
        .categoryId(category.getId())
        .category(category.getCategoryName())
        .tags(tags)
        .accessStatus(request.getAccessStatus())
        .branchPermission(request.getBranchPermission())
        .steps(draft.getSteps())
        .parentDetails(Objects.nonNull(draft.getParentTemplateId()) ?
            ParentDetails.builder().parentId(draft.getParentTemplateId()).build() : null)
        .createTime(LocalDateTime.now())
        .build();
  }


  /**
   * Build {@link TrackedTemplate} from scratch.
   * Original template is persisted in a simplified {@link com.test.template.models.complete.SimpleCompleteTemplate} object.
   * {@link com.test.template.models.steps.TrackedStep}'s are created with just a NOT_STARTED property.
   * Should be persisted only with content.
   *
   * @param completeTemplate a completed template
   * @param username         the user who is tracking the template and is currently logged in
   */
  public TrackedTemplate buildNewTrackedTemplate(final CompleteTemplate completeTemplate, final String username) {
    List<TemplateStep> steps = completeTemplate.getSteps();
    return TrackedTemplate.builder()
        .originalTemplate(templateMapper.toSimpleTemplate(completeTemplate))
        .creationTime(LocalDateTime.now())
        .ownerName(username)
        .trackedSteps(steps.stream().map(stepMapper::toEmptyTrackedStep).toList())
        .currentStatus(ProgressionStatus.NOT_STARTED)
        .creationTime(LocalDateTime.now())
        .build();
  }

}
