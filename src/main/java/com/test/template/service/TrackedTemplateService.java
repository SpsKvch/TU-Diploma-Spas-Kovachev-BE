package com.test.template.service;

import com.test.security.jwt.JwtUtil;
import com.test.template.exceptions.TemplateException;
import com.test.template.mappers.TemplateStepMapper;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.tracked.TrackedTemplate;
import com.test.template.models.tracked.UpdateTrackedTemplateRequest;
import com.test.template.repository.CompleteTemplateRepository;
import com.test.template.repository.TrackedTemplateRepository;
import com.test.template.service.builders.TemplateBuilder;
import com.test.template.service.helpers.ProgressionStatusHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TrackedTemplateService {

  private static final String NO_TEMPLATES_FOUND_FOR_ID = "No template with id %s found";
  private static final String NO_TRACKED_TEMPLATE_FOUND_FOR_ID = "No tracked template draft with id %s found";
  private static final String STEPS_SIZE_MISMATCH = "Size of updated steps exceeds that of the saved steps";

  private final TemplateBuilder templateBuilder;
  private final ProgressionStatusHelper progressionStatusHelper;
  private final TemplateStepMapper stepMapper;
  private final CompleteTemplateRepository templateRepository;
  private final TrackedTemplateRepository trackedTemplateRepository;

  public TrackedTemplate trackTemplate(final String templateId) {
    String loggedInUser = JwtUtil.getLoggedInUser();
    if (trackedTemplateRepository.existsByOwnerNameAndOriginalTemplateId(loggedInUser, templateId)) {
      log.info("User {} already has a journal for template {}", loggedInUser, templateId);
      return null;
    }

    CompleteTemplate template = templateRepository.findById(templateId).orElseThrow(
        () -> new TemplateException(String.format(NO_TEMPLATES_FOUND_FOR_ID, templateId), HttpStatus.BAD_REQUEST));
    TrackedTemplate trackedTemplate = templateBuilder.buildNewTrackedTemplate(template, JwtUtil.getLoggedInUser());
    log.info("Creating new journal from template: {}", templateId);
    return trackedTemplateRepository.insert(trackedTemplate);
  }

  public void updateTrackedTemplate(final UpdateTrackedTemplateRequest request, final String trackedTemplateId) {
    TrackedTemplate trackedTemplate = trackedTemplateRepository.findById(trackedTemplateId)
        .orElseThrow(() -> new TemplateException(String.format(NO_TRACKED_TEMPLATE_FOUND_FOR_ID, trackedTemplateId),
            HttpStatus.BAD_REQUEST));

    trackedTemplate.setNewTitle(request.getNewTitle());
    trackedTemplate.setMarkedUpContent(request.getMarkedUpContent());

    if (Objects.nonNull(request.getTrackedRequirements())) {
      trackedTemplate.setTrackedRequirements(request.getTrackedRequirements());
    }

    if (request.getTrackedSteps().keySet().size() > trackedTemplate.getTrackedSteps().size()) {
      throw new TemplateException(STEPS_SIZE_MISMATCH, HttpStatus.BAD_REQUEST);
    }

    Map<Integer, TrackedStep> requestSteps = request.getTrackedSteps().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> stepMapper.toTrackedStep(entry.getValue())));
    progressionStatusHelper.validateStatusApplicability(requestSteps.values());

    List<TrackedStep> trackedSteps = trackedTemplate.getTrackedSteps();

    for (Map.Entry<Integer, TrackedStep> step : requestSteps.entrySet()) {
      TrackedStep currentStep = trackedSteps.get(step.getKey());
      currentStep.setMarkedUpTitle(step.getValue().getMarkedUpTitle());
      currentStep.setMarkedUpContent(step.getValue().getMarkedUpContent());
      currentStep.setProgressionStatus(step.getValue().getProgressionStatus());
      currentStep.setTimeSpent(step.getValue().getTimeSpent());
      currentStep.setNotes(step.getValue().getNotes());
    }

    //Set status
    progressionStatusHelper.validateProgressionStepOrdering(trackedSteps);
    ProgressionStatus aggregateStatus = progressionStatusHelper.aggregateStepProgressionStatus(trackedSteps);
    if (!Objects.isNull(request.getNewStatus()) && request.getNewStatus().isTemplateLevel()) {
      progressionStatusHelper.checkProgressionStatusApplicability(request.getNewStatus(), aggregateStatus);
      trackedTemplate.setCurrentStatus(request.getNewStatus());
    } else {
      trackedTemplate.setCurrentStatus(aggregateStatus);
    }

    trackedTemplate.setUpdateTime(LocalDateTime.now());

    log.info("Updating journal with id: {}", trackedTemplateId);
    trackedTemplateRepository.save(trackedTemplate);
  }

  public TrackedTemplate getJournalFromTemplateForOwner(String templateId, String ownerName) {
    validateJournalOwnership(ownerName);

    Optional<TrackedTemplate> retrievedJournal = trackedTemplateRepository
        .findByOwnerNameAndOriginalTemplateId(ownerName, templateId);
    if (retrievedJournal.isEmpty()) {
      throw new TemplateException(String.format(
          "User %s has not created a journal for template with id: %s", ownerName, templateId), HttpStatus.NOT_FOUND);
    }

    return retrievedJournal.get();
  }

  public List<TrackedTemplate> getJournalsForUser(String ownerName) {
    validateJournalOwnership(ownerName);
    return trackedTemplateRepository.getTrackedTemplatesByOwnerName(ownerName);
  }

  public boolean deleteTrackedTemplate(final String username, final String trackedTemplateId) {
    if (!username.equals(JwtUtil.getLoggedInUser())) {
      throw new TemplateException(String.format("User %s does not own this template", username), HttpStatus.BAD_REQUEST);
    }

    log.info("Deleting journal with id: {} and owner: {}", trackedTemplateId, username);
    return trackedTemplateRepository.deleteDistinctByIdAndOwnerName(trackedTemplateId, username) > 0;
  }

  private void validateJournalOwnership(String ownerName) {
    String loggedInUser = JwtUtil.getLoggedInUser();
    if (!ownerName.equals(loggedInUser)) {
      throw new TemplateException("Journal(s) does not belong to calling user", HttpStatus.FORBIDDEN);
    }
  }
}
