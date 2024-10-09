package com.test.template.service;

import com.test.security.jwt.JwtUtil;
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
import com.test.utils.ThreadExecutor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.test.template.validation.ValidationConstants.MIN_STEP_COUNT;

@Slf4j
@Service
@AllArgsConstructor
public class CompleteTemplateService {

  private static final String USER_DOES_NOT_OWN_TEMPLATE = "User %s is not the creator of this template";
  private static final String TOO_FEW_REQUIRED_STEPS = "Template must have " + MIN_STEP_COUNT + " steps at minimum";
  private static final String NO_CATEGORY_FOUND = "Unable to find requested category";
  private static final String NO_TEMPLATES_FOUND_FOR_ID = "No template with id %s found";
  private static final String NO_DRAFT_FOUND_FOR_ID = "No template draft with id %s found";
  private static final String TAGS_NOT_APPLICABLE = "The following tags are not applicable to category %s: %s";
  private static final String TAG_SEPARATOR = ", ";

  private final AccessValidator accessValidator;
  private final TemplateBuilder templateBuilder;
  private final TemplateAccessHelper templateAccessHelper;
  private final TemplateQueryService templateQueryService;
  private final CompleteTemplateRepository templateRepository;
  private final TemplateDraftRepository draftRepository;
  private final CategoryRepository categoryRepository;
  private final TemplateUserRepository userRepository;

  public CompleteTemplate createTemplateFromDraft(final CreateCompleteTemplateRequest request,
                                                  final String draftId) {
    TemplateDraft draft = draftRepository.findById(draftId).orElseThrow(() ->
            new TemplateException(String.format(NO_DRAFT_FOUND_FOR_ID, draftId), HttpStatus.BAD_REQUEST));

    if (draft.getSteps().size() < MIN_STEP_COUNT) {
      throw new TemplateException(TOO_FEW_REQUIRED_STEPS, HttpStatus.BAD_REQUEST);
    }

    String user = JwtUtil.getLoggedInUser();
    if (!draft.getCreatorName().equals(user)) {
      throw new TemplateException(String.format("Draft %s does not belong to user %s", draftId, user), HttpStatus.FORBIDDEN);
    }

    Set<String> lowerCaseTags = request.getTags().stream()
        .map(String::toLowerCase)
        .collect(Collectors.toSet());

    Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
            () -> new TemplateException(NO_CATEGORY_FOUND, HttpStatus.BAD_REQUEST));
    if (!category.getChildTags().containsAll(lowerCaseTags)) {
      throw new TemplateException(buildTagNotApplicableErrorMessage(category, lowerCaseTags), HttpStatus.BAD_REQUEST);
    }

    accessValidator.validateStatuses(request.getAccessStatus(), request.getBranchPermission());
    accessValidator.validateStatuses(request.getAccessStatus(), request.getBranchPermission());
    if (!StringUtils.isBlank(draft.getParentTemplateId())) {
      templateRepository.findById(draft.getParentTemplateId()).ifPresent((parent) ->
              accessValidator.validateStatusInRelationToParent(request.getAccessStatus(), parent.getAccessStatus()));
    }

    CompleteTemplate template = templateBuilder.buildTemplateFromDraft(request, draft, category, lowerCaseTags);
    CompleteTemplate createdTemplate = templateRepository.insert(template);
    log.info("Promoting draft: {} to full template", draftId);

    ThreadExecutor.executeTask(() -> {
      draftRepository.deleteById(draftId);
      log.info("Deleted promoted draft: {}", draftId);
      templateRepository.incrementBranches(createdTemplate.getId());
    });

    return createdTemplate;
  }

  public void updateTemplateStatuses(final ChangeAccessRequest request, final String templateId) {
    CompleteTemplate template = fetchTemplateById(templateId);
    if (!template.getCreatorName().equals(JwtUtil.getLoggedInUser())) {
      throw new TemplateException(String.format(USER_DOES_NOT_OWN_TEMPLATE, JwtUtil.getLoggedInUser()), HttpStatus.FORBIDDEN);
    }

    if (Objects.nonNull(request.getAccessStatus())) {
      template.setAccessStatus(request.getAccessStatus());
    }
    if (Objects.nonNull(request.getBranchPermission())) {
      template.setBranchPermission(request.getBranchPermission());
    }

    accessValidator.validateStatuses(template.getAccessStatus(), template.getBranchPermission());
    templateRepository.save(template);
  }

  /**
   * Creates a new {@link TemplateDraft} based on the parameters of an existing {@link CompleteTemplate}
   *
   * @param parentId - The id of the parent template
   * @return {@link TemplateDraft} - The new draft branched from the parent
   */
  public TemplateDraft createTemplateDraftFromParent(final String parentId) {
    CompleteTemplate parentTemplate = fetchTemplateById(parentId);
    TemplateUser creator = userRepository.getTemplateUserByUsername(parentTemplate.getCreatorName()).orElse(null);

    if (!templateAccessHelper.templateCanBeBranched(parentTemplate, creator)) {
      throw new TemplateException("Unable to branch template", HttpStatus.BAD_REQUEST);
    }

    TemplateDraft createdDraft = templateBuilder.buildDraftFromParent(parentTemplate, JwtUtil.getLoggedInUser());
    return draftRepository.insert(createdDraft);
  }

  public boolean checkBranchAvailability(final String templateId) {
    CompleteTemplate template = fetchTemplateById(templateId);
    TemplateUser creator = userRepository.getTemplateUserByUsername(template.getCreatorName()).orElse(null);

    return templateAccessHelper.templateCanBeBranched(template, creator);
  }

  public CompleteTemplate getTemplateById(final String templateId) {
    log.info("Fetching template with id: {}", templateId);
    CompleteTemplate retrievedTemplate = templateRepository.findById(templateId)
            .orElseThrow(() -> new TemplateException(String.format(NO_TEMPLATES_FOUND_FOR_ID, templateId), HttpStatus.NOT_FOUND));

    if (!templateAccessHelper.templateCanBeAccessed(retrievedTemplate)) {
      throw new TemplateException(String.format("Unable to access template: %s", templateId), HttpStatus.FORBIDDEN);
    }

    ThreadExecutor.executeTask(() -> templateRepository.incrementTemplateViews(templateId));
    return retrievedTemplate;
  }

  public List<CompleteTemplate> getTemplatesFromCreator(final String username, int page, int elementsPerPage) {
    if (page == 0 && elementsPerPage == 0) {
      log.info("Fetching all templates created by user: {}", username);
      return templateRepository.getCompleteTemplatesByCreatorName(username);
    }
    log.info("Fetching {} templates from page {}", elementsPerPage, page);
    return templateRepository.getCompleteTemplatesByCreatorNameAndAccessStatus(username, AccessStatus.PUBLIC, PageRequest.of(page, elementsPerPage));
  }

  public Page<CompleteTemplate> getPublicTemplatesPage(final int page, final int elementsPerPage) {
    Pageable pageable;
    Sort sortOrder = Sort.by(Sort.Direction.DESC, "createTime");

    if (page == 0 || elementsPerPage == 0) {
      pageable = PageRequest.of(0, Integer.MAX_VALUE, sortOrder);
      //return templateRepository.getAllByAccessStatus(AccessStatus.PUBLIC, Pageable.unpaged(), Sort.by(Sort.Direction.DESC, "createTime"));
    } else {
      pageable = PageRequest.of(page, elementsPerPage, sortOrder);
    }

    return templateRepository.getAllByAccessStatus(AccessStatus.PUBLIC, pageable);
  }

  public Page<CompleteTemplate> getPublicTemplatesFiltered(final CompleteTemplateFilters filters) {
    return templateQueryService.findCompleteTemplatesFiltered(filters);
  }

  public void alterApprovals(String templateId, boolean approved) {
    TemplateUser user = userRepository.getTemplateUserByUsername(JwtUtil.getLoggedInUser()).orElse(null);
    if (Objects.isNull(user)) {
      return;
    }

    Map<String, Boolean> likedTemplates = user.getLikedTemplates();

    Pair<Integer, Integer> incrementationPair;
    if (!likedTemplates.containsKey(templateId)) { // Add like or dislike
      likedTemplates.put(templateId, approved);
      incrementationPair = approved ? new ImmutablePair<>(1, 1) : new ImmutablePair<>(0, 1);
    } else {
      if (likedTemplates.get(templateId)) {
        if (approved) { // Remove like if template has already been liked
          likedTemplates.remove(templateId);
          incrementationPair = new ImmutablePair<>(-1, -1);
        } else { // Remove like and add dislike
          likedTemplates.put(templateId, false);
          incrementationPair = new ImmutablePair<>(-1, 0);
        }
      } else {
        if (approved) { //Add like and remove dislike
          likedTemplates.put(templateId, true);
          incrementationPair = new ImmutablePair<>(1, 0);
        } else { //Remove dislike
          likedTemplates.remove(templateId);
          incrementationPair = new ImmutablePair<>(0, -1);
        }
      }
    }

    userRepository.save(user);
    templateRepository.incrementApprovalsAndEngagements(templateId, incrementationPair.getLeft(),
            incrementationPair.getRight());
  }

  public void updateSharedWith(final String templateId, final List<String> sharedWith) {
    if (sharedWith.contains(JwtUtil.getLoggedInUser())) {
      throw new TemplateException("User cannot share template with themself", HttpStatus.BAD_REQUEST);
    }
    templateRepository.updateSharedWith(templateId, sharedWith);
  }

  public void deleteTemplate(String templateId) {
    log.info("Deleting complete template with id: {}", templateId);
    templateRepository.deleteById(templateId);
  }

  private String buildTagNotApplicableErrorMessage(Category category, Set<String> requestTags) {
    StringBuilder sb = new StringBuilder();
    requestTags.removeAll(category.getChildTags());
    requestTags.forEach(tag -> sb.append(tag).append(TAG_SEPARATOR));
    return String.format(TAGS_NOT_APPLICABLE, category.getCategoryName(),
            sb.substring(0, sb.length() - TAG_SEPARATOR.length()));
  }

  private CompleteTemplate fetchTemplateById(String templateId) {
    return templateRepository.findById(templateId)
            .orElseThrow(() -> new TemplateException(String.format(NO_TEMPLATES_FOUND_FOR_ID, templateId), HttpStatus.NOT_FOUND));
  }

}
