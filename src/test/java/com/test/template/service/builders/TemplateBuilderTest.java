package com.test.template.service.builders;

import com.test.template.mappers.TemplateMapper;
import com.test.template.mappers.TemplateStepMapper;
import com.test.template.models.categorization.Category;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CreateCompleteTemplateRequest;
import com.test.template.models.complete.SimpleCompleteTemplate;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.draft.TemplateDraftRequest;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.tracked.TrackedTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


import static com.test.utils.ObjectsUtil.ID;
import static com.test.utils.ObjectsUtil.MARKED_UP_CONTENT;
import static com.test.utils.ObjectsUtil.MARKED_UP_IMAGE;
import static com.test.utils.ObjectsUtil.MARKED_UP_TITLE;
import static com.test.utils.ObjectsUtil.TAG_NAME;
import static com.test.utils.ObjectsUtil.UPDATED_CONTENT;
import static com.test.utils.ObjectsUtil.UPDATED_IMAGE_URL;
import static com.test.utils.ObjectsUtil.UPDATED_TITLE;
import static com.test.utils.ObjectsUtil.USER;
import static com.test.utils.ObjectsUtil.createCategory;
import static com.test.utils.ObjectsUtil.createCompleteTemplate;
import static com.test.utils.ObjectsUtil.createCompleteTemplateRequest;
import static com.test.utils.ObjectsUtil.createDraft;
import static com.test.utils.ObjectsUtil.createDraftRequest;
import static com.test.utils.ObjectsUtil.createJournal;
import static com.test.utils.ObjectsUtil.createSimpleCompleteTemplate;
import static com.test.utils.ObjectsUtil.createStep;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateBuilderTest {

  @Mock
  private TemplateStepMapper stepMapper;
  @Mock
  private TemplateMapper templateMapper;
  @InjectMocks
  private TemplateBuilder templateBuilder;

  @Test
  void buildTemplateDraft_validRequest_DraftCreated() {
    LocalDateTime now = LocalDateTime.now();
    TemplateDraft draft = createDraft();
    draft.setId(null);
    draft.setTitle(UPDATED_TITLE);
    draft.setContent(UPDATED_CONTENT);
    draft.setImageUrl(UPDATED_IMAGE_URL);
    draft.setCreationTime(now);
    draft.getSteps().remove(1);

    TemplateDraftRequest request = createDraftRequest();
    request.setRequirements(draft.getRequirements());

    MockedStatic<LocalDateTime> mockedDate = mockStatic(LocalDateTime.class);
    mockedDate.when(LocalDateTime::now).thenReturn(now);
    when(stepMapper.toStep(any())).thenReturn(createStep(true, false));

    var result = templateBuilder.buildTemplateDraft(request, USER);

    assertEquals(draft, result);

    mockedDate.close();
  }

  @Test
  void buildDraftFromParent_ValidRequest_DraftCreated() {
    LocalDateTime now = LocalDateTime.now();
    CompleteTemplate template = createCompleteTemplate();
    TemplateDraft draft = createDraft();
    draft.setId(null);
    draft.setCreationTime(now);
    draft.setParentTemplateId(ID);

    MockedStatic<LocalDateTime> mockedDate = mockStatic(LocalDateTime.class);
    mockedDate.when(LocalDateTime::now).thenReturn(now);

    var result = templateBuilder.buildDraftFromParent(template, USER);

    assertEquals(draft, result);

    mockedDate.close();
  }

  @Test
  void buildDraftFromJournal_ValidDraftIgnoreAbandoned_DraftCreated() {
    LocalDateTime now = LocalDateTime.now();

    TrackedTemplate journal = createJournal();

    TrackedStep firstCompleteStep = journal.getTrackedSteps().get(0);
    firstCompleteStep.setProgressionStatus(ProgressionStatus.COMPLETED);
    TrackedStep abandonedStep = new TrackedStep();
    abandonedStep.setProgressionStatus(ProgressionStatus.ABANDONED);
    TrackedStep secondCompleteStep = journal.getTrackedSteps().get(1);
    secondCompleteStep.setProgressionStatus(ProgressionStatus.COMPLETED);

    journal.setOriginalTemplate(createSimpleCompleteTemplate());
    journal.setTrackedSteps(List.of(firstCompleteStep, abandonedStep, secondCompleteStep));

    TemplateDraft draft = createDraft();
    draft.setId(null);
    draft.setTitle(MARKED_UP_TITLE);
    draft.setContent(MARKED_UP_CONTENT);
    draft.setImageUrl(MARKED_UP_IMAGE);
    draft.getSteps().get(0).setTitle("First"+MARKED_UP_TITLE);
    draft.getSteps().get(0).setContent("First"+MARKED_UP_CONTENT);
    draft.getSteps().get(1).setTitle("Second"+MARKED_UP_TITLE);
    draft.getSteps().get(1).setContent("Second"+MARKED_UP_CONTENT);
    draft.setCreationTime(now);

    MockedStatic<LocalDateTime> mockedDate = mockStatic(LocalDateTime.class);
    mockedDate.when(LocalDateTime::now).thenReturn(now);

    var result = templateBuilder.buildDraftFromJournal(USER, journal, true);

    assertEquals(draft, result);

    mockedDate.close();
  }

  @Test
  void buildTemplateFromDraft_ValidRequest_DraftCreated() {
    LocalDateTime now = LocalDateTime.now();

    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();
    TemplateDraft draft = createDraft();
    Category category = createCategory();

    CompleteTemplate expectedTemplate = createCompleteTemplate();
    expectedTemplate.setId(null);
    expectedTemplate.setSharedWith(request.getSharedWith());
    expectedTemplate.setCreateTime(now);

    MockedStatic<LocalDateTime> mockedDate = mockStatic(LocalDateTime.class);
    mockedDate.when(LocalDateTime::now).thenReturn(now);

    var result = templateBuilder.buildTemplateFromDraft(request, draft, category, Set.of(TAG_NAME));

    assertEquals(expectedTemplate, result);

    mockedDate.close();
  }

  @Test
  void buildNewTrackedTemplate_ValidTemplate_JournalCreated() {
    LocalDateTime now = LocalDateTime.now();

    CompleteTemplate template = createCompleteTemplate();
    template.getSteps().remove(1);
    SimpleCompleteTemplate simpleTemplate = createSimpleCompleteTemplate();
    TrackedTemplate journal = createJournal();
    TrackedStep emptyStep = new TrackedStep();
    emptyStep.setProgressionStatus(ProgressionStatus.NOT_STARTED);

    journal.setId(null);
    journal.setNewTitle(null);
    journal.setMarkedUpContent(null);
    journal.setMarkedUpImage(null);
    journal.setOriginalTemplate(simpleTemplate);
    journal.setTrackedSteps(List.of(emptyStep));
    journal.setTrackedRequirements(null);
    journal.setCurrentStatus(ProgressionStatus.NOT_STARTED);
    journal.setCreationTime(now);

    MockedStatic<LocalDateTime> mockedDate = mockStatic(LocalDateTime.class);
    mockedDate.when(LocalDateTime::now).thenReturn(now);
    when(templateMapper.toSimpleTemplate(template)).thenReturn(simpleTemplate);
    when(stepMapper.toEmptyTrackedStep(template.getSteps().get(0))).thenReturn(emptyStep);

    var result = templateBuilder.buildNewTrackedTemplate(template, USER);

    assertEquals(journal, result);

    mockedDate.close();
  }

}