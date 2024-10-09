package com.test.template.service;

import com.test.template.exceptions.TemplateException;
import com.test.template.mappers.TemplateStepMapper;
import com.test.template.models.draft.PutTemplateDraftRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.draft.TemplateDraftRequest;
import com.test.template.models.tracked.TrackedTemplate;
import com.test.template.repository.TemplateDraftRepository;
import com.test.template.repository.TrackedTemplateRepository;
import com.test.template.service.builders.TemplateBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import static com.test.utils.ObjectsUtil.ID;
import static com.test.utils.ObjectsUtil.SECOND_USER;
import static com.test.utils.ObjectsUtil.USER;
import static com.test.utils.ObjectsUtil.createDraft;
import static com.test.utils.ObjectsUtil.createDraftRequest;
import static com.test.utils.ObjectsUtil.createJournal;
import static com.test.utils.ObjectsUtil.createPutDraftRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateDraftServiceTest {

  private Authentication authentication = Mockito.mock(Authentication.class);
  private SecurityContext securityContext = Mockito.mock(SecurityContext.class);
  @Mock
  private TemplateBuilder templateBuilder;
  @Mock
  private TemplateStepMapper stepMapper;
  @Mock
  private TemplateDraftRepository draftRepository;
  @Mock
  private TrackedTemplateRepository trackedTemplateRepository;
  @InjectMocks
  private TemplateDraftService templateDraftService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createTemplateDraft_ValidRequest_Success() {
    TemplateDraftRequest request = createDraftRequest();
    TemplateDraft draft = createDraft();

    mockSecurity(USER);
    when(templateBuilder.buildTemplateDraft(request, USER)).thenReturn(draft);
    when(draftRepository.insert(draft)).thenReturn(draft);

    var result = templateDraftService.createTemplateDraft(request);

    assertEquals(draft, result);
  }

  @Test
  void updateTemplateDraft_ValidRequest_DraftUpdated() {
    PutTemplateDraftRequest request = createPutDraftRequest();
    TemplateDraft draft = createDraft();

    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));
    when(stepMapper.toStep(request.getSteps().get(0))).thenReturn(draft.getSteps().get(0));

    templateDraftService.updateTemplateDraft(request, ID);

    assertEquals(request.getTitle(), draft.getTitle());
    assertEquals(request.getContent(), draft.getContent());
    assertEquals(request.getImageUrl(), draft.getImageUrl());
    assertEquals(request.getRequirements(), draft.getRequirements());

    verify(draftRepository, times(1)).save(draft);
  }

  @Test
  void updateTemplateDraft_EmptyRequest_OnlyImageUpdated() {
    PutTemplateDraftRequest request = new PutTemplateDraftRequest();
    TemplateDraft draft = createDraft();

    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    templateDraftService.updateTemplateDraft(request, ID);

    assertNotNull(draft.getTitle());
    assertNotNull(draft.getContent());
    assertNull(draft.getImageUrl());
    assertNotNull(draft.getSteps());
    assertNotNull(draft.getRequirements());

    verify(stepMapper, never()).toStep(any());
    verify(draftRepository, times(1)).save(draft);
  }

  @Test
  void updateTemplateDraft_NotDraftFound_Exception() {
    when(draftRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> templateDraftService.updateTemplateDraft(new PutTemplateDraftRequest(), ID));
  }

  @Test
  void promoteTrackedTemplateToDraft_JournalFound_Success() {
    TrackedTemplate journal = createJournal();
    TemplateDraft draft = createDraft();

    mockSecurity(USER);
    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.of(journal));
    when(templateBuilder.buildDraftFromJournal(anyString(), any(), anyBoolean())).thenReturn(draft);
    when(draftRepository.insert(draft)).thenReturn(draft);

    var result = templateDraftService.promoteTrackedTemplateToDraft(ID, true, true);

    assertEquals(draft, result);
    verify(trackedTemplateRepository, after(100)).delete(journal);
  }

  @Test
  void promoteTrackedTemplateToDraft_NoDraftFound_Exception() {
    mockSecurity(USER);
    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> templateDraftService.promoteTrackedTemplateToDraft(ID, true, true));
  }

  @Test
  void promoteTrackedTemplateToDraft_NonOwner_Exception() {
    TrackedTemplate journal = createJournal();
    journal.setOwnerName(SECOND_USER);

    mockSecurity(USER);
    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.of(journal));

    assertThrows(TemplateException.class, () -> templateDraftService.promoteTrackedTemplateToDraft(ID, true, true));
  }

  @Test
  void getTemplateDraftsFromCreator_OwnerAndPages_Success() {
    List<TemplateDraft> drafts = Collections.singletonList(createDraft());

    mockSecurity(USER);
    when(draftRepository.getTemplateDraftsByCreatorName(anyString(), any())).thenReturn(drafts);

    var result = templateDraftService.getTemplateDraftsFromCreator(USER, 1, 1);

    assertEquals(drafts, result);
    verify(draftRepository, never()).getTemplateDraftsByCreatorName(anyString());
  }

  @Test
  void getTemplateDraftsFromCreator_OwnerAndNoPages_Success() {
    List<TemplateDraft> drafts = Collections.singletonList(createDraft());

    mockSecurity(USER);
    when(draftRepository.getTemplateDraftsByCreatorName(anyString())).thenReturn(drafts);

    var result = templateDraftService.getTemplateDraftsFromCreator(USER, 0, 0);

    assertEquals(drafts, result);
    verify(draftRepository, never()).getTemplateDraftsByCreatorName(anyString(), any());
  }

  @Test
  void getTemplateDraftsFromCreator_NotOwner_Exception() {
    mockSecurity(USER);

    assertThrows(TemplateException.class, () -> templateDraftService.getTemplateDraftsFromCreator(SECOND_USER, 0, 0));
  }

  @Test
  void getTemplateDraftById_FoundDraftAndIsOwner_Success() {
    TemplateDraft draft = createDraft();

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    var result = templateDraftService.getTemplateDraftById(ID);

    assertEquals(draft, result);
  }

  @Test
  void getTemplateDraftById_FoundDraftAndNotOwner_Exception() {
    TemplateDraft draft = createDraft();
    draft.setCreatorName(SECOND_USER);

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    assertThrows(TemplateException.class, () -> templateDraftService.getTemplateDraftById(ID));
  }

  @Test
  void getTemplateDraftById_NoDraftFound_Exception() {
    when(draftRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> templateDraftService.getTemplateDraftById(ID));
  }

  @Test
  void deleteTemplateDraft_FoundDraftAndIsOwner_ReturnsTrue() {
    TemplateDraft draft = createDraft();

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    var result = templateDraftService.deleteTemplateDraft(ID);

    assertTrue(result);
  }

  @Test
  void deleteTemplateDraft_FoundDraftAndIsNotOwner_ReturnsFalse() {
    TemplateDraft draft = createDraft();
    draft.setCreatorName(SECOND_USER);

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    var result = templateDraftService.deleteTemplateDraft(ID);

    assertFalse(result);
  }

  @Test
  void deleteTemplateDraft_DraftNotFound_ReturnsFalse() {
    when(draftRepository.findById(ID)).thenReturn(Optional.empty());

    var result = templateDraftService.deleteTemplateDraft(ID);

    assertFalse(result);
  }

  private void mockSecurity(String username) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }

}