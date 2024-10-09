package com.test.template.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.template.models.complete.ChangeAccessRequest;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CompleteTemplateFilters;
import com.test.template.models.complete.CreateCompleteTemplateRequest;
import com.test.template.models.draft.TemplateDraft;
import com.test.template.service.CompleteTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1")
public class CompleteTemplateController {

  private final CompleteTemplateService templateService;
  private final ObjectMapper objectMapper;

  @Operation(description = "Converts draft to completed template")
  @PostMapping("/templates/drafts/{templateDraftId}/complete")
  public CompleteTemplate createTemplate(@Valid @RequestBody final CreateCompleteTemplateRequest request,
                                         @NotBlank @PathVariable final String templateDraftId) {
    return templateService.createTemplateFromDraft(request, templateDraftId);
  }

  @Operation(description = "Creates a draft based on an existing complete template")
  @PostMapping("/templates/complete/{templateId}/drafts")
  public TemplateDraft createDraftFromTemplate(@NotBlank @PathVariable final String templateId) {
    return templateService.createTemplateDraftFromParent(templateId);
  }

  @Operation(description = "Adjusts template draft access status and permission")
  @PatchMapping("/templates/complete/{templateId}/access")
  public void updateTemplateStatuses(@NotBlank @PathVariable String templateId,
                                     @RequestBody ChangeAccessRequest changeAccessRequest) {
    templateService.updateTemplateStatuses(changeAccessRequest, templateId);
  }

  @Operation(description = "Like or dislike a given template")
  @PatchMapping("/templates/complete/{templateId}/approve")
  public void changeTemplateApproval(@NotBlank @PathVariable String templateId,
                                     @NotNull @RequestParam Boolean approved) {
    templateService.alterApprovals(templateId, approved);
  }

  @Operation(description = "Update users template is shared with")
  @PatchMapping("/templates/complete/{templateId}/sharedWith")
  public void updateTemplateSharing(@NotBlank @PathVariable String templateId,
                                    @NotNull @RequestBody List<String> sharedWith) {
    templateService.updateSharedWith(templateId, sharedWith);
  }

  @Operation(description = "Get complete template by id")
  @GetMapping("/templates/complete/{templateId}")
  public CompleteTemplate getTemplateById(@NotBlank @PathVariable final String templateId) {
    return templateService.getTemplateById(templateId);
  }

  @Operation(description = "Get page of most recent templates")
  @GetMapping("/templates/complete")
  public Page<CompleteTemplate> getTemplatePage(@RequestParam(required = false, defaultValue = "0") final int page,
                                                @RequestParam(required = false, defaultValue = "0") final int elementsPerPage) {
    return templateService.getPublicTemplatesPage(page, elementsPerPage);
  }

  @Operation(description = "Get templates via passed in filters")
  @GetMapping("/templates/complete/filtered")
  public Page<CompleteTemplate> getTemplatesFiltered(@RequestParam final String filters) throws Exception {
    CompleteTemplateFilters mappedFilters = objectMapper.readValue(filters, CompleteTemplateFilters.class);
    return templateService.getPublicTemplatesFiltered(mappedFilters);
  }

  @Operation(description = "Get all complete templates from user")
  @GetMapping("/users/{username}/templates/complete")
  public List<CompleteTemplate> getTemplatesCreatedByUser(@NotBlank @PathVariable final String username,
                                                          @RequestParam(required = false, defaultValue = "0") final int page,
                                                          @RequestParam(required = false, defaultValue = "0") final int elementsPerPage) {
    return templateService.getTemplatesFromCreator(username, page, elementsPerPage);
  }

  @Operation(description = "Get branch availability for template")
  @GetMapping("/templates/complete/{templateId}/branch")
  public boolean checkIfTemplateCanBeBranched(@PathVariable @NotBlank String templateId) {
    return templateService.checkBranchAvailability(templateId);
  }

  @Operation(description = "Get tags belonging to template")
  @GetMapping("/templates/complete/{templateId}/tags")
  public Set<String> getAllTagsForTemplate(@NotBlank @PathVariable String templateId) {
    return getTemplateById(templateId).getTags();
  }

  @DeleteMapping("/templates/complete/{templateId}")
  public void deleteTemplate(@NotBlank @PathVariable final String templateId) {
    templateService.deleteTemplate(templateId);
  }

}
