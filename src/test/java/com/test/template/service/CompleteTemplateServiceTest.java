package com.test.template.service;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.categorization.Category;
import com.test.template.models.complete.ChangeAccessRequest;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CompleteTemplateFilters;
import com.test.template.models.complete.CreateCompleteTemplateRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.enums.AccessStatus;
import com.test.template.repository.CategoryRepository;
import com.test.template.repository.CompleteTemplateRepository;
import com.test.template.repository.TemplateDraftRepository;
import com.test.template.service.builders.TemplateBuilder;
import com.test.template.service.helpers.TemplateAccessHelper;
import com.test.template.validation.AccessValidator;
import com.test.user.models.TemplateUser;
import com.test.user.repository.TemplateUserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import static com.test.utils.ObjectsUtil.CATEGORY_ID;
import static com.test.utils.ObjectsUtil.ID;
import static com.test.utils.ObjectsUtil.SECOND_USER;
import static com.test.utils.ObjectsUtil.USER;
import static com.test.utils.ObjectsUtil.createCategory;
import static com.test.utils.ObjectsUtil.createChangeAccessRequest;
import static com.test.utils.ObjectsUtil.createCompleteTemplate;
import static com.test.utils.ObjectsUtil.createCompleteTemplateRequest;
import static com.test.utils.ObjectsUtil.createDraft;
import static com.test.utils.ObjectsUtil.createUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteTemplateServiceTest {

  private Authentication authentication = Mockito.mock(Authentication.class);
  private SecurityContext securityContext = Mockito.mock(SecurityContext.class);

  @Mock
  private AccessValidator accessValidator;
  @Mock
  private TemplateBuilder templateBuilder;
  @Mock
  private TemplateAccessHelper templateAccessHelper;
  @Mock
  private TemplateQueryService templateQueryService;
  @Mock
  private CompleteTemplateRepository templateRepository;
  @Mock
  private TemplateDraftRepository draftRepository;
  @Mock
  private CategoryRepository categoryRepository;
  @Mock
  private TemplateUserRepository userRepository;
  @InjectMocks
  private CompleteTemplateService completeTemplateService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createTemplateFromDraft_ValidRequest_Success() {
    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();

    TemplateDraft draft = createDraft();
    draft.setParentTemplateId(ID);

    CompleteTemplate template = new CompleteTemplate();
    template.setId(ID);
    template.setAccessStatus(AccessStatus.PUBLIC);

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));
    when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(createCategory()));
    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(templateBuilder.buildTemplateFromDraft(any(),any(), any(), any())).thenReturn(template);
    when(templateRepository.insert(template)).thenReturn(template);

    var result = completeTemplateService.createTemplateFromDraft(request, ID);

    assertEquals(template, result);
    verify(draftRepository, after(100)).deleteById(ID);
    verify(templateRepository).incrementBranches(template.getId());
  }

  @Test
  void createTemplateFromDraft_NoDraft_Exception() {
    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();

    when(draftRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> completeTemplateService.createTemplateFromDraft(request, ID));
  }

  @Test
  void createTemplateFromDraft_TooFewSteps_Exception() {
    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();

    TemplateDraft draft = createDraft();
    draft.setParentTemplateId(ID);
    draft.getSteps().remove(1);

    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    assertThrows(TemplateException.class, () -> completeTemplateService.createTemplateFromDraft(request, ID));
  }

  @Test
  void createTemplateFromDraft_NotOwner_Exception() {
    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();

    TemplateDraft draft = createDraft();
    draft.setParentTemplateId(ID);

    CompleteTemplate template = new CompleteTemplate();
    template.setId(ID);
    template.setAccessStatus(AccessStatus.PUBLIC);

    mockSecurity(SECOND_USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));

    assertThrows(TemplateException.class, () -> completeTemplateService.createTemplateFromDraft(request, ID));
  }

  @Test
  void createTemplateFromDraft_NoCategory_Exception() {
    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();

    TemplateDraft draft = createDraft();
    draft.setParentTemplateId(ID);

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));
    when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> completeTemplateService.createTemplateFromDraft(request, ID));
  }

  @Test
  void createTemplateFromDraft_NonMatchingTags_Exception() {
    CreateCompleteTemplateRequest request = createCompleteTemplateRequest();

    TemplateDraft draft = createDraft();
    draft.setParentTemplateId(ID);

    Category category = createCategory();
    category.setChildTags(Set.of("Missing tag"));

    mockSecurity(USER);
    when(draftRepository.findById(ID)).thenReturn(Optional.of(draft));
    when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

    assertThrows(TemplateException.class, () -> completeTemplateService.createTemplateFromDraft(request, ID));
  }

  @Test
  void updateTemplateStatuses_ValidRequest_Success() {
    ChangeAccessRequest request = createChangeAccessRequest();
    CompleteTemplate template = createCompleteTemplate();

    mockSecurity(USER);
    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));

    completeTemplateService.updateTemplateStatuses(request, ID);

    verify(templateRepository).save(template);
  }

  @Test
  void updateTemplateStatuses_NoTemplate_Exception() {
    ChangeAccessRequest request = createChangeAccessRequest();

    when(templateRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> completeTemplateService.updateTemplateStatuses(request, ID));
  }

  @Test
  void updateTemplateStatuses_NotOwner_Exception() {
    ChangeAccessRequest request = createChangeAccessRequest();
    CompleteTemplate template = createCompleteTemplate();
    template.setCreatorName(SECOND_USER);

    mockSecurity(USER);
    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));

    assertThrows(TemplateException.class, () -> completeTemplateService.updateTemplateStatuses(request, ID));
  }

  @Test
  void createTemplateDraftFromParent_HasParent_Success() {
    CompleteTemplate template = createCompleteTemplate();
    TemplateUser user = createUser();
    TemplateDraft draft = createDraft();

    mockSecurity(USER);
    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(userRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));
    when(templateAccessHelper.templateCanBeBranched(template, user)).thenReturn(true);
    when(templateBuilder.buildDraftFromParent(template, USER)).thenReturn(draft);
    when(draftRepository.insert(draft)).thenReturn(draft);

    var result = completeTemplateService.createTemplateDraftFromParent(ID);

    assertEquals(draft, result);
  }

  @Test
  void createTemplateDraftFromParent_HasParentCannotBeBranched_Exception() {
    CompleteTemplate template = createCompleteTemplate();
    TemplateUser user = createUser();

    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(userRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));
    when(templateAccessHelper.templateCanBeBranched(template, user)).thenReturn(false);

    assertThrows(TemplateException.class, () -> completeTemplateService.createTemplateDraftFromParent(ID));
  }

  @Test
  void checkBranchAvailability_TemplateExists_Success() {
    CompleteTemplate template = createCompleteTemplate();
    TemplateUser user = createUser();

    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(userRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));
    when(templateAccessHelper.templateCanBeBranched(template, user)).thenReturn(true);

    var result = completeTemplateService.checkBranchAvailability(ID);

    assertTrue(result);
  }

  @Test
  void getTemplateById_TemplateExists_Success() {
    CompleteTemplate template = createCompleteTemplate();

    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(templateAccessHelper.templateCanBeAccessed(template)).thenReturn(true);

    var result = completeTemplateService.getTemplateById(ID);

    assertEquals(template, result);
    verify(templateRepository, after(100)).incrementTemplateViews(ID);
  }

  @Test
  void getTemplateById_NoTemplateExists_Exception() {
    when(templateRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> completeTemplateService.getTemplateById(ID));
  }

  @Test
  void getTemplateById_TemplateCannotBeAccessed_Exception() {
    CompleteTemplate template = createCompleteTemplate();

    when(templateRepository.findById(ID)).thenReturn(Optional.of(template));
    when(templateAccessHelper.templateCanBeAccessed(template)).thenReturn(false);

    assertThrows(TemplateException.class, () -> completeTemplateService.getTemplateById(ID));
  }

  @Test
  void getTemplatesFromCreator_NonZeroPages_Success() {
    List<CompleteTemplate> templates = Collections.singletonList(createCompleteTemplate());

    when(templateRepository.getCompleteTemplatesByCreatorNameAndAccessStatus(anyString(), any(), any())).thenReturn(templates);

    var result = completeTemplateService.getTemplatesFromCreator(USER, 1, 1);

    assertEquals(templates, result);
  }

  @Test
  void getTemplatesFromCreator_NoPages_Success() {
    List<CompleteTemplate> templates = Collections.singletonList(createCompleteTemplate());

    when(templateRepository.getCompleteTemplatesByCreatorName(USER)).thenReturn(templates);

    var result = completeTemplateService.getTemplatesFromCreator(USER, 0, 0);

    assertEquals(templates, result);
  }

  @Test
  void getPublicTemplatesPage_NonZeroPages_Success() {
    Page<CompleteTemplate> templates = Page.empty();

    Sort sortOrder = Sort.by(Sort.Direction.DESC, "createTime");
    Pageable pageable = PageRequest.of(1, 1, sortOrder);

    when(templateRepository.getAllByAccessStatus(AccessStatus.PUBLIC, pageable)).thenReturn(templates);

    var result = completeTemplateService.getPublicTemplatesPage(1, 1);

    assertEquals(templates, result);
  }

  @Test
  void getPublicTemplatesPage_NoPages_Success() {
    Page<CompleteTemplate> templates = Page.empty();

    Sort sortOrder = Sort.by(Sort.Direction.DESC, "createTime");
    Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sortOrder);

    when(templateRepository.getAllByAccessStatus(AccessStatus.PUBLIC, pageable)).thenReturn(templates);

    var result = completeTemplateService.getPublicTemplatesPage(0, 0);

    assertEquals(templates, result);
  }

  @Test
  void getPublicTemplatesFiltered_ValidFilters_Success() {
    Page<CompleteTemplate> templates = Page.empty();

    when(templateQueryService.findCompleteTemplatesFiltered(any())).thenReturn(templates);

    var result = completeTemplateService.getPublicTemplatesFiltered(new CompleteTemplateFilters());

    assertEquals(templates, result);
  }

  @Test
  void alterApprovals_NoUserFound_NoInteractions() {
    mockSecurity(USER);
    when(userRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.empty());

    completeTemplateService.alterApprovals(ID, true);

    verify(templateRepository, never()).incrementApprovalsAndEngagements(anyString(), anyInt(), anyInt());
    verify(userRepository, never()).save(any());
  }

  @Test
  void alterApprovals_NoApprovalForTemplate_ApprovalAdded() {
    alterApprovalsBaseTest(null, true);
    alterApprovalsBaseTest(null, false);
    alterApprovalsBaseTest(true, true);
    alterApprovalsBaseTest(true, false);
    alterApprovalsBaseTest(false, true);
    alterApprovalsBaseTest(false, false);

    verify(templateRepository).incrementApprovalsAndEngagements(ID, 1, 1);
    verify(templateRepository).incrementApprovalsAndEngagements(ID, 0, 1);
    verify(templateRepository).incrementApprovalsAndEngagements(ID, -1, -1);
    verify(templateRepository).incrementApprovalsAndEngagements(ID, -1, 0);
    verify(templateRepository).incrementApprovalsAndEngagements(ID, 1, 0);
    verify(templateRepository).incrementApprovalsAndEngagements(ID, 0, -1);
    verify(userRepository, times(6)).save(any());
  }

  private void alterApprovalsBaseTest(Boolean templateApproved, boolean approval) {
    TemplateUser user = createUser();

    Map<String, Boolean> templateMap = new HashMap<>();;
    if (templateApproved != null) {
      templateMap.put(ID, templateApproved);
    }
    user.setLikedTemplates(templateMap);

    mockSecurity(USER);
    when(userRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));

    completeTemplateService.alterApprovals(ID, approval);
  }

  @Test
  void updateSharedWith_NotSharedAlready_Success() {
    mockSecurity(USER);

    completeTemplateService.updateSharedWith(ID, List.of(SECOND_USER));

    verify(templateRepository, times(1)).updateSharedWith(ID, List.of(SECOND_USER));
  }

  @Test
  void updateSharedWith_AlreadyShared_Exception() {
    mockSecurity(USER);

    assertThrows(TemplateException.class, () -> completeTemplateService.updateSharedWith(ID, List.of(USER)));
  }

  @Test
  void deleteTemplate_AnyId_Success() {
    completeTemplateService.deleteTemplate(ID);
    verify(templateRepository, times(1)).deleteById(ID);
  }

  private void mockSecurity(String username) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }
}