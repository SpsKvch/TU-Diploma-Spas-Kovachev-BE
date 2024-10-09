package com.test.template.service;

import com.test.template.exceptions.TemplateException;
import com.test.template.mappers.TemplateStepMapper;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TemplateStep;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.tracked.TrackedTemplate;
import com.test.template.models.tracked.UpdateTrackedTemplateRequest;
import com.test.template.repository.CompleteTemplateRepository;
import com.test.template.repository.TrackedTemplateRepository;
import com.test.template.service.builders.TemplateBuilder;
import com.test.template.service.helpers.ProgressionStatusHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static com.test.utils.ObjectsUtil.createCompleteTemplate;
import static com.test.utils.ObjectsUtil.createJournal;
import static com.test.utils.ObjectsUtil.createTrackedStepsList;
import static com.test.utils.ObjectsUtil.createUpdateJournalRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackedTemplateServiceTest {

  private Authentication authentication = Mockito.mock(Authentication.class);
  private SecurityContext securityContext = Mockito.mock(SecurityContext.class);
  @Mock
  private TemplateBuilder templateBuilder;
  @Mock
  private ProgressionStatusHelper progressionStatusHelper;
  @Mock
  private TemplateStepMapper stepMapper;
  @Mock
  private CompleteTemplateRepository templateRepository;
  @Mock
  private TrackedTemplateRepository trackedTemplateRepository;
  @InjectMocks
  private TrackedTemplateService trackedTemplateService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void trackTemplate_TemplateExists_JournalCreated() {
    CompleteTemplate template = createCompleteTemplate();
    TrackedTemplate journal = createJournal();

    mockSecurity(USER);
    when(trackedTemplateRepository.existsByOwnerNameAndOriginalTemplateId(USER, ID)).thenReturn(false);
    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(templateBuilder.buildNewTrackedTemplate(template, USER)).thenReturn(journal);
    when(trackedTemplateRepository.insert(journal)).thenReturn(journal);

    var result = trackedTemplateService.trackTemplate(ID);

    assertEquals(journal, result);
  }

  @Test
  void trackTemplate_NoTemplateExists_Exception() {
    mockSecurity(USER);
    when(trackedTemplateRepository.existsByOwnerNameAndOriginalTemplateId(USER, ID)).thenReturn(false);
    when(templateRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> trackedTemplateService.trackTemplate(ID));
  }

  @Test
  void trackTemplate_JournalExistsForUser_Null() {
    mockSecurity(USER);
    when(trackedTemplateRepository.existsByOwnerNameAndOriginalTemplateId(USER, ID)).thenReturn(true);

    var result = trackedTemplateService.trackTemplate(ID);

    assertNull(result);
  }

  @Test
  void updateTrackedTemplate_ValidParameters_JournalUpdated() {
    UpdateTrackedTemplateRequest request = createUpdateJournalRequest();
    TrackedTemplate journal = createJournal();
    TrackedStep mappedStep = createTrackedStepsList().get(0);

    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.of(journal));
    when(stepMapper.toTrackedStep(any())).thenReturn(mappedStep);
    when(progressionStatusHelper.aggregateStepProgressionStatus(any())).thenReturn(ProgressionStatus.IN_PROGRESS);

    trackedTemplateService.updateTrackedTemplate(request, ID);

    verify(progressionStatusHelper, times(1)).validateStatusApplicability(any());
    verify(progressionStatusHelper, times(1)).validateProgressionStepOrdering(anyList());
    verify(progressionStatusHelper, times(1)).checkProgressionStatusApplicability(any(), any());
    verify(trackedTemplateRepository, times(1)).save(journal);
  }

  @Test
  void updateTrackedTemplate_EmptyRequest_JournalUpdated() {
    UpdateTrackedTemplateRequest request = new UpdateTrackedTemplateRequest();
    request.setTrackedSteps(new HashMap<>());

    TrackedTemplate journal = createJournal();
    ProgressionStatus expectedStatus = ProgressionStatus.IN_PROGRESS;

    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.of(journal));
    when(progressionStatusHelper.aggregateStepProgressionStatus(any())).thenReturn(expectedStatus);

    trackedTemplateService.updateTrackedTemplate(request, ID);

    assertNull(journal.getNewTitle());
    assertNull(journal.getMarkedUpContent());
    assertNotNull(journal.getTrackedRequirements());
    assertEquals(journal.getCurrentStatus(), expectedStatus);

    verify(progressionStatusHelper, times(1)).validateStatusApplicability(any());
    verify(progressionStatusHelper, times(1)).validateProgressionStepOrdering(anyList());
    verify(progressionStatusHelper, times(1)).aggregateStepProgressionStatus(any());
    verify(trackedTemplateRepository, times(1)).save(journal);
  }

  @Test
  void updateTrackedTemplate_TooManySteps_Exception() {
    UpdateTrackedTemplateRequest request = createUpdateJournalRequest();
    TrackedTemplate journal = createJournal();
    journal.setTrackedSteps(new ArrayList<>());

    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.of(journal));

    assertThrows(TemplateException.class, () -> trackedTemplateService.updateTrackedTemplate(request, ID));
  }

  @Test
  void updateTrackedTemplate_NoJournalFound_Exception() {
    UpdateTrackedTemplateRequest request = createUpdateJournalRequest();

    when(trackedTemplateRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> trackedTemplateService.updateTrackedTemplate(request, ID));
  }

  @Test
  void getJournalFromTemplateForOwner_UserIsOwner_JournalReturned() {
    TrackedTemplate journal = createJournal();

    mockSecurity(USER);
    when(trackedTemplateRepository.findByOwnerNameAndOriginalTemplateId(USER, ID)).thenReturn(Optional.of(journal));

    var result = trackedTemplateService.getJournalFromTemplateForOwner(ID, USER);

    assertEquals(journal, result);
  }

  @Test
  void getJournalFromTemplateForOwner_JournalNotFound_Exception() {
    mockSecurity(USER);
    when(trackedTemplateRepository.findByOwnerNameAndOriginalTemplateId(USER, ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> trackedTemplateService.getJournalFromTemplateForOwner(ID, USER));
  }

  @Test
  void getJournalFromTemplateForOwner_NotOwner_Exception() {
    mockSecurity(USER);

    assertThrows(TemplateException.class, () -> trackedTemplateService.getJournalFromTemplateForOwner(ID, SECOND_USER));
  }

  @Test
  void getJournalsForUser_UserIsOwner_JournalsRetrieved() {
    List<TrackedTemplate> journals = Collections.singletonList(createJournal());

    mockSecurity(USER);
    when(trackedTemplateRepository.getTrackedTemplatesByOwnerName(USER)).thenReturn(journals);

    var result = trackedTemplateService.getJournalsForUser(USER);

    assertEquals(journals, result);
  }

  @Test
  void getJournalsForUser_UserIsNotOwner_Exception() {

    mockSecurity(USER);

    assertThrows(TemplateException.class, () -> trackedTemplateService.getJournalsForUser(SECOND_USER));
  }

  @Test
  void deleteTrackedTemplate_UserIsOwner_JournalIsDeleted() {
    mockSecurity(USER);
    when(trackedTemplateRepository.deleteDistinctByIdAndOwnerName(ID, USER)).thenReturn(1L);

    var result = trackedTemplateService.deleteTrackedTemplate(USER, ID);
    assertTrue(result);
  }

  @Test
  void deleteTrackedTemplate_UserIsNotOwner_Exception() {
    mockSecurity(USER);

    assertThrows(TemplateException.class, () -> trackedTemplateService.deleteTrackedTemplate(SECOND_USER, ID));
  }

  private void mockSecurity(String username) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }

}