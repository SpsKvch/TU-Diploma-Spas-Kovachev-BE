package com.test.template.controller;

import com.test.template.models.draft.PutTemplateDraftRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.models.draft.TemplateDraftRequest;
import com.test.template.service.TemplateDraftService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1")
public class TemplateDraftController {

  private final TemplateDraftService draftService;

  @Operation(description = "Creates a new template draft. Either original or from parent")
  @PostMapping("/templates/drafts")
  public TemplateDraft createTemplateDraft(@RequestBody final TemplateDraftRequest draftRequest) {
    return draftService.createTemplateDraft(draftRequest);
  }

  @Operation(description = "Creates draft from a journal")
  @PostMapping("/templates/drafts/{trackedTemplateId}")
  public TemplateDraft promoteJournalToDraft(@NotBlank @PathVariable final String trackedTemplateId,
      @RequestParam final boolean ignoreAbandoned,
      @RequestParam(defaultValue = "true") final boolean deleteOnCreation) {
    return draftService.promoteTrackedTemplateToDraft(trackedTemplateId, ignoreAbandoned, deleteOnCreation);
  }

  @Operation(description = "Updates template draft")
  @PutMapping("/templates/drafts/{draftId}")
  public void updateTemplateDraft(@Valid @RequestBody final PutTemplateDraftRequest templateDraftRequest,
      @NotBlank @PathVariable final String draftId) {
    draftService.updateTemplateDraft(templateDraftRequest, draftId);
  }

  @Operation(description = "Get template draft by id")
  @GetMapping("/templates/drafts/{templateDraftId}")
  public TemplateDraft getTemplateDraftById(@NotBlank @PathVariable final String templateDraftId) {
    return draftService.getTemplateDraftById(templateDraftId);
  }

  @Operation(description = "Get all draft created by user")
  @GetMapping("/users/{username}/templates/drafts")
  public List<TemplateDraft> getTemplateDraftsByUser(@NotBlank @PathVariable final String username,
      @RequestParam(required = false, defaultValue = "0") final int page,
      @RequestParam(required = false, defaultValue = "0") final int elementsPerPage) {
    return draftService.getTemplateDraftsFromCreator(username, page, elementsPerPage);
  }

  @Operation(description = "Delete draft for user")
  @DeleteMapping("/templates/drafts/{draftId}")
  public ResponseEntity<Boolean> deleteTemplateDraft(@NotBlank @PathVariable final String draftId) {
    return new ResponseEntity<>(draftService.deleteTemplateDraft(draftId), HttpStatus.OK);
  }

}
