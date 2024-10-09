package com.test.template.service;

import com.test.security.jwt.JwtUtil;
import com.test.template.exceptions.TemplateException;
import com.test.template.mappers.TemplateStepMapper;
import com.test.template.models.draft.PutTemplateDraftRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.draft.TemplateDraftRequest;
import com.test.template.models.tracked.TrackedTemplate;
import com.test.template.repository.TemplateDraftRepository;
import com.test.template.repository.TrackedTemplateRepository;
import com.test.template.service.builders.TemplateBuilder;
import com.test.utils.ThreadExecutor;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class TemplateDraftService {
  private static final String USER_DOES_NOT_OWN_DRAFT = "User %s does not own this draft";
  private static final String USER_DOES_NOT_OWN_JOURNAL = "User does not own this journal";
  private static final String NO_DRAFT_FOUND_FOR_ID = "No template draft with id %s found";
  private static final String NO_TRACKED_TEMPLATE_FOUND_FOR_ID = "No tracked template draft with id %s found";

  private final TemplateBuilder templateBuilder;
  private final TemplateStepMapper stepMapper;
  private final TemplateDraftRepository draftRepository;
  private final TrackedTemplateRepository trackedTemplateRepository;

  public TemplateDraft createTemplateDraft(TemplateDraftRequest request) {
    TemplateDraft draft = templateBuilder.buildTemplateDraft(request, JwtUtil.getLoggedInUser());
    log.info("Creating original template draft for user {}", JwtUtil.getLoggedInUser());
    return draftRepository.insert(draft);
  }

  public void updateTemplateDraft(PutTemplateDraftRequest request, String draftId) {
    TemplateDraft originalDraft = draftRepository.findById(draftId).orElseThrow(() ->
            new TemplateException(String.format(NO_DRAFT_FOUND_FOR_ID, draftId), HttpStatus.BAD_REQUEST));
    if (!StringUtils.isBlank(request.getTitle())) {
      originalDraft.setTitle(request.getTitle());
    }

    if (!StringUtils.isBlank(request.getContent())) {
      originalDraft.setContent(request.getContent());
    }

    if (Objects.nonNull(request.getSteps())) {
      originalDraft.setSteps(request.getSteps().stream()
          .map(stepMapper::toStep)
          .toList());
    }

    if (Objects.nonNull(request.getRequirements())) {
      originalDraft.setRequirements(request.getRequirements());
    }

    originalDraft.setImageUrl(request.getImageUrl());
    originalDraft.setLastUpdateTime(LocalDateTime.now());
    log.info("Updating template draft with id: {}", draftId);
    draftRepository.save(originalDraft);
  }

  public TemplateDraft promoteTrackedTemplateToDraft(final String trackedTemplateId, final boolean ignoreAbandoned,
                                                     final boolean deleteOnCreation) {
    String owner = JwtUtil.getLoggedInUser();
    TrackedTemplate journal = trackedTemplateRepository.findById(trackedTemplateId).orElseThrow(
            () -> new TemplateException(String.format(NO_TRACKED_TEMPLATE_FOUND_FOR_ID, trackedTemplateId), HttpStatus.BAD_REQUEST));

    if (!Objects.requireNonNull(owner).equals(journal.getOwnerName())) {
      throw new TemplateException(USER_DOES_NOT_OWN_JOURNAL, HttpStatus.BAD_REQUEST);
    }

    TemplateDraft promotedDraft = templateBuilder.buildDraftFromJournal(owner, journal, ignoreAbandoned);
    log.info("Promoting journal: {} to draft", trackedTemplateId);
    TemplateDraft createdDraft = draftRepository.insert(promotedDraft);

    if (deleteOnCreation) {
      ThreadExecutor.executeTask(() -> {
        log.info("Deleting tracked template after creating draft {}", createdDraft.getId());
        trackedTemplateRepository.delete(journal);
      });
    }

    return createdDraft;
  }

  public List<TemplateDraft> getTemplateDraftsFromCreator(String username, int page, int elementsPerPage) {
    if (!username.equals(JwtUtil.getLoggedInUser())) {
      throw new TemplateException(String.format("Unable to view drafts from user %s", username), HttpStatus.BAD_REQUEST);
    }

    if (page == 0 && elementsPerPage == 0) {
      log.info("Fetching all drafts created by: {}", username);
      return draftRepository.getTemplateDraftsByCreatorName(username);
    }

    log.info("Fetching {} drafts from page {}", elementsPerPage, page);
    return draftRepository.getTemplateDraftsByCreatorName(username, PageRequest.of(page, elementsPerPage));
  }

  public TemplateDraft getTemplateDraftById(final String templateDraftId) {
    log.info("Fetching draft with id: {}", templateDraftId);
    TemplateDraft retrievedDraft = draftRepository.findById(templateDraftId)
            .orElseThrow(() -> new TemplateException(String.format(NO_DRAFT_FOUND_FOR_ID, templateDraftId), HttpStatus.NOT_FOUND));

    String loggedInUser = Objects.requireNonNull(JwtUtil.getLoggedInUser());
    if (!loggedInUser.equals(retrievedDraft.getCreatorName())) {
      throw new TemplateException(String.format(USER_DOES_NOT_OWN_DRAFT, loggedInUser), HttpStatus.FORBIDDEN);
    }

    return retrievedDraft;
  }

  public boolean deleteTemplateDraft(String templateDraftId) {
    TemplateDraft retrievedDraft = draftRepository.findById(templateDraftId).orElse(null);

    if (retrievedDraft == null) {
      return false;
    }

    if (retrievedDraft.getCreatorName().equals(JwtUtil.getLoggedInUser())) {
      draftRepository.deleteById(templateDraftId);
      log.info("Successfully delete template draft with id: {} for user: {}", templateDraftId, JwtUtil.getLoggedInUser());
      return true;
    }

    return false;
  }
}
