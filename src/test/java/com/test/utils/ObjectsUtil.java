package com.test.utils;

import com.test.template.models.Requirement;
import com.test.template.models.categorization.Category;
import com.test.template.models.complete.ChangeAccessRequest;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CompleteTemplateFilters;
import com.test.template.models.complete.CreateCompleteTemplateRequest;
import com.test.template.models.complete.SimpleCompleteTemplate;
import com.test.template.models.draft.PutTemplateDraftRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.draft.TemplateDraftRequest;
import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import com.test.template.models.enums.ProgressionStatus;
import com.test.template.models.steps.TemplateStep;
import com.test.template.models.steps.TemplateStepRequest;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.steps.TrackedStepRequest;
import com.test.template.models.tracked.TrackedTemplate;

import com.test.template.models.tracked.UpdateTrackedTemplateRequest;
import com.test.user.models.CreateTemplateUserRequest;
import com.test.user.models.FriendRequest;
import com.test.user.models.TemplateUser;
import com.test.user.models.TemplateUserResponse;
import com.test.user.models.UpdateTemplateUserRequest;
import com.test.user.models.groups.UserGroup;
import com.test.user.models.groups.UserGroupRequest;
import com.test.user.service.impl.UserGroupService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectsUtil {

  public static final String ID = "id";
  public static final String USER = "USER";
  public static final String SECOND_USER = "SECOND_USER";
  public static final String TITLE = "Title";
  public static final String CONTENT = "Content";
  public static final String IMAGE_URL = "url";
  public static final String MARKED_UP_TITLE = "Marked Up Title";
  public static final String MARKED_UP_CONTENT = "Marked Up Content";
  public static final String MARKED_UP_IMAGE = "Marked Up Image";
  public static final String UPDATED_TITLE = "Updated Title";
  public static final String UPDATED_CONTENT = "Updated Content";
  public static final String UPDATED_IMAGE_URL = "Updated Image Url";
  public static final String CATEGORY_ID = "id";
  public static final String CATEGORY_NAME = "Name";
  public static final String TAG_NAME = "tag";
  public static final String FIRST_NAME = "First Name";
  public static final String LAST_NAME = "Last Name";
  public static final String EMAIL = "email@mail.to";
  public static final String COUNTRY = "country";
  public static final String REGION = "region";
  public static final String RAW_GROUP_NAME = "group";
  public static final String GROUP_NAME = "Group";
  public static final String DESCRIPTION = "description";

  private static final Duration FIXED_DURATION = Duration.of(20, ChronoUnit.MINUTES);

  //Core objects
  public static CompleteTemplate createCompleteTemplate() {
    CompleteTemplate completeTemplate = new CompleteTemplate();
    completeTemplate.setId(ID);
    completeTemplate.setTitle(TITLE);
    completeTemplate.setContent(CONTENT);
    completeTemplate.setImageUrl(IMAGE_URL);
    completeTemplate.setCreatorName(USER);
    completeTemplate.setAccessStatus(AccessStatus.PUBLIC);
    completeTemplate.setBranchPermission(BranchPermission.ALL);
    completeTemplate.setCategory(CATEGORY_NAME);
    completeTemplate.setTags(Set.of(TAG_NAME));
    completeTemplate.setSteps(createSteps());
    completeTemplate.setRequirements(createRequirements());
    return completeTemplate;
  }

  public static SimpleCompleteTemplate createSimpleCompleteTemplate() {
    SimpleCompleteTemplate simpleCompleteTemplate = new SimpleCompleteTemplate();
    simpleCompleteTemplate.setTitle(TITLE);
    simpleCompleteTemplate.setContent(CONTENT);
    simpleCompleteTemplate.setImageUrl(IMAGE_URL);
    simpleCompleteTemplate.setCreatorName(USER);
    simpleCompleteTemplate.setRequirements(createRequirements());
    simpleCompleteTemplate.setSteps(createSteps());
    return simpleCompleteTemplate;
  }

  public static TemplateDraft createDraft() {
    TemplateDraft draft = new TemplateDraft();
    draft.setId(ID);
    draft.setTitle(TITLE);
    draft.setContent(CONTENT);
    draft.setImageUrl(IMAGE_URL);
    draft.setCreatorName(USER);
    draft.setSteps(createSteps());
    draft.setRequirements(createRequirements());
    return draft;
  }

  public static List<TemplateStep> createSteps() {
    List<TemplateStep> steps = new ArrayList<>();
    steps.add(createStep(true, false));
    steps.add(createStep(false, true));
    return steps;
  }

  public static TemplateStep createStep(boolean important, boolean optional) {
    TemplateStep step = new TemplateStep();
    step.setTitle(TITLE);
    step.setContent(CONTENT);
    step.setImageUrl(IMAGE_URL);
    step.setMinTimeEstimate(Duration.ofMinutes(10));
    step.setMaxTimeEstimate(Duration.ofMinutes(20));
    step.setImportant(important);
    step.setOptional(optional);
    return step;
  }

  public static Set<Requirement> createRequirements() {
    Set<Requirement> requirements = new HashSet<>();
    requirements.add(Requirement.builder().content(CONTENT).optional(false).build());
    requirements.add(Requirement.builder().content(CONTENT).optional(true).build());
    return requirements;
  }

  public static TrackedTemplate createJournal() {
    TrackedTemplate journal = new TrackedTemplate();
    journal.setId(ID);
    journal.setNewTitle(MARKED_UP_TITLE);
    journal.setMarkedUpContent(MARKED_UP_CONTENT);
    journal.setMarkedUpImage(MARKED_UP_IMAGE);
    journal.setOwnerName(USER);
    journal.setTrackedSteps(createTrackedStepsList());
    journal.setTrackedRequirements(createRequirements());
    return journal;
  }

  public static List<TrackedStep> createTrackedStepsList() {
    List<TrackedStep> stepList = new ArrayList<>();
    stepList.add(TrackedStep.builder()
        .markedUpTitle("First" + MARKED_UP_TITLE)
        .markedUpContent("First" + MARKED_UP_CONTENT)
        .timeSpent(FIXED_DURATION)
        .markedUpImage(IMAGE_URL)
        .progressionStatus(ProgressionStatus.IN_PROGRESS)
        .build());
    stepList.add(TrackedStep.builder()
        .markedUpTitle("Second" + MARKED_UP_TITLE)
        .markedUpContent("Second" + MARKED_UP_CONTENT)
        .timeSpent(FIXED_DURATION)
        .markedUpImage(IMAGE_URL)
        .progressionStatus(ProgressionStatus.IN_PROGRESS)
        .build());
    stepList.add(TrackedStep.builder()
        .markedUpTitle("Third" + MARKED_UP_TITLE)
        .markedUpContent("Third" + MARKED_UP_CONTENT)
        .timeSpent(FIXED_DURATION)
        .progressionStatus(ProgressionStatus.IN_PROGRESS)
        .build());
    stepList.add(TrackedStep.builder()
        .markedUpTitle("Fourth" + MARKED_UP_TITLE)
        .markedUpContent("Fourth" + MARKED_UP_CONTENT)
        .timeSpent(FIXED_DURATION)
        .build());
    stepList.add(TrackedStep.builder()
        .markedUpTitle("Fifth" + MARKED_UP_TITLE)
        .markedUpContent("Fifth" + MARKED_UP_CONTENT)
        .timeSpent(FIXED_DURATION)
        .progressionStatus(ProgressionStatus.IN_PROGRESS)
        .build());
    return stepList;
  }

  public static Map<Integer, TrackedStepRequest> createTrackedRequirements() {
    Map<Integer, TrackedStepRequest> updatedSteps = new HashMap<>();
    updatedSteps.put(0, new TrackedStepRequest(UPDATED_TITLE, UPDATED_CONTENT, "Image", new ArrayList<>(), Duration.parse("PT10M"), ProgressionStatus.COMPLETED));
    updatedSteps.put(1, new TrackedStepRequest(UPDATED_TITLE, UPDATED_CONTENT, "Image", new ArrayList<>(), Duration.of(10, ChronoUnit.MINUTES), ProgressionStatus.ABANDONED));
    updatedSteps.put(2, new TrackedStepRequest(UPDATED_TITLE, UPDATED_CONTENT, "Image", new ArrayList<>(), Duration.parse("PT2H"), ProgressionStatus.IN_PROGRESS));
    return updatedSteps;
  }

  public static Category createCategory() {
    Category category = new Category();
    category.setCategoryName(CATEGORY_NAME);
    category.setChildTags(Set.of(TAG_NAME));
    return category;
  }

  public static TemplateUser createUser() {
    TemplateUser user = new TemplateUser();
    user.setId(ID);
    user.setUsername(USER);
    user.setFirstName(FIRST_NAME);
    user.setLastName(LAST_NAME);
    user.setCurrentEmail(EMAIL);
    user.setProfileImageUrl(IMAGE_URL);
    user.setCountry(COUNTRY);
    user.setRegion(REGION);
    user.setFriends(new ArrayList<>(List.of(SECOND_USER)));
    return user;
  }

  public static FriendRequest createFriendRequest() {
    FriendRequest friendRequest = new FriendRequest();
    friendRequest.setRecipient(USER);
    friendRequest.setSender(SECOND_USER);
    return friendRequest;
  }

  public static UserGroup createUserGroup() {
    UserGroup userGroup = new UserGroup();
    userGroup.setCreator(USER);
    userGroup.setMembers(new HashSet<>(Set.of(USER)));
    userGroup.setGroupNameRaw(RAW_GROUP_NAME);
    userGroup.setGroupNameActual(GROUP_NAME);
    userGroup.setDescription(DESCRIPTION);
    return userGroup;
  }

  //Requests
  public static CreateCompleteTemplateRequest createCompleteTemplateRequest() {
    CreateCompleteTemplateRequest request = new CreateCompleteTemplateRequest();
    request.setAccessStatus(AccessStatus.PUBLIC);
    request.setBranchPermission(BranchPermission.ALL);
    request.setTags(Set.of(TAG_NAME));
    request.setCategoryId(CATEGORY_ID);
    request.setSharedWith(Set.of(SECOND_USER));
    return request;
  }

  public static TemplateDraftRequest createDraftRequest() {
    TemplateDraftRequest request = new TemplateDraftRequest();
    request.setTitle(UPDATED_TITLE);
    request.setContent(UPDATED_CONTENT);
    request.setImageUrl(UPDATED_IMAGE_URL);
    request.setRequirements(new HashSet<>());
    request.setSteps(new ArrayList<>(List.of(createStepRequest())));
    return request;
  }

  public static TemplateStepRequest createStepRequest() {
    TemplateStepRequest request = new TemplateStepRequest();
    request.setTitle(TITLE);
    request.setContent(CONTENT);
    request.setImportant(true);
    request.setOptional(true);
    request.setMinTimeEstimate(FIXED_DURATION);
    request.setMaxTimeEstimate(FIXED_DURATION.plus(Duration.ofMinutes(10)));
    return request;
  }

  public static PutTemplateDraftRequest createPutDraftRequest() {
    PutTemplateDraftRequest request = new PutTemplateDraftRequest();
    request.setTitle(UPDATED_TITLE);
    request.setContent(UPDATED_CONTENT);
    request.setImageUrl(UPDATED_IMAGE_URL);
    request.setRequirements(new HashSet<>());
    request.setSteps(List.of(new TemplateStepRequest()));
    return request;
  }

  public static UpdateTrackedTemplateRequest createUpdateJournalRequest() {
    UpdateTrackedTemplateRequest request = new UpdateTrackedTemplateRequest();
    request.setNewTitle(MARKED_UP_TITLE);
    request.setMarkedUpContent(MARKED_UP_CONTENT);
    request.setNewStatus(ProgressionStatus.ON_HOLD);
    request.setTrackedSteps(createTrackedStepsRequest());
    request.setTrackedRequirements(createRequirements());
    return request;
  }

  public static Map<Integer, TrackedStepRequest> createTrackedStepsRequest() {
    Map<Integer, TrackedStepRequest> trackedSteps = new HashMap<>();

    TrackedStepRequest trackedStepRequest = new TrackedStepRequest();
    trackedStepRequest.setTitle(MARKED_UP_TITLE);
    trackedStepRequest.setMarkedUpContent(MARKED_UP_CONTENT);
    trackedStepRequest.setTimeSpent(FIXED_DURATION);
    trackedStepRequest.setMarkedUpImage(MARKED_UP_IMAGE);
    trackedStepRequest.setNotes(List.of(CONTENT));
    trackedStepRequest.setProgressionStatus(ProgressionStatus.IN_PROGRESS);

    trackedSteps.put(0, trackedStepRequest);
    return trackedSteps;
  }

  public static ChangeAccessRequest createChangeAccessRequest() {
    ChangeAccessRequest request = new ChangeAccessRequest();
    request.setAccessStatus(AccessStatus.PRIVATE);
    request.setBranchPermission(BranchPermission.NONE);
    return request;
  }

  public static CreateTemplateUserRequest createCreateTemplateUserRequest() {
    CreateTemplateUserRequest request = new CreateTemplateUserRequest();
    request.setFirstName(FIRST_NAME);
    request.setLastName(LAST_NAME);
    request.setEmail(EMAIL);
    request.setCountry(COUNTRY);
    request.setRegion(REGION);
    return request;
  }

  public static UpdateTemplateUserRequest createUpdateTemplateUserRequest() {
    UpdateTemplateUserRequest request = new UpdateTemplateUserRequest();
    request.setFirstName(FIRST_NAME);
    request.setLastName(LAST_NAME);
    request.setCountry(COUNTRY);
    request.setRegion(REGION);
    request.setProfileImageUrl(IMAGE_URL);
    request.setNewEmail(EMAIL);
    return request;
  }

  public static TemplateUserResponse createTemplateUserResponse() {
    TemplateUserResponse response = new TemplateUserResponse();
    response.setUsername(USER);
    response.setProfileImageUrl(IMAGE_URL);
    response.setCountry(COUNTRY);
    return response;
  }

  public static UserGroupRequest createUserGroupRequest() {
    UserGroupRequest userGroupRequest = new UserGroupRequest();
    userGroupRequest.setName(GROUP_NAME);
    userGroupRequest.setDescription(DESCRIPTION);
    return userGroupRequest;
  }

  //Other
  public static CompleteTemplateFilters createTemplateFilters() {
    CompleteTemplateFilters filters = new CompleteTemplateFilters();
    filters.setCategoryName(CATEGORY_NAME);
    filters.setTags(Set.of(TAG_NAME));
    filters.setIsOriginal(true);
    filters.setTitle(TITLE);
    filters.setMinDate(LocalDate.now().minusYears(1));
    filters.setMaxDate(LocalDate.now().plusMonths(1));
    filters.setMaxCompletionTime(Duration.of(30, ChronoUnit.DAYS));
    filters.setMinApprovalPercent(1);
    filters.setMinCompletionRate(5);
    return filters;
  }

}
