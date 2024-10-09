package com.test.template.controller;

import com.test.template.models.tracked.TrackedTemplate;
import com.test.template.models.tracked.UpdateTrackedTemplateRequest;
import com.test.template.service.TrackedTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/v1")
public class TrackedTemplateController {

  private static final String PRESENT_JOURNAL_REDIRECT_URI = "/templates/journal/";

  private final TrackedTemplateService trackedTemplateService;

  @Operation(description = "Creates draft based on an existing completed template")
  @PostMapping("templates/complete/{templateId}/journal")
  public ResponseEntity<TrackedTemplate> createTrackedTemplate(@NotBlank @PathVariable final String templateId) {
    TrackedTemplate createdJournal = trackedTemplateService.trackTemplate(templateId);
    if (createdJournal == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT)
          .header("Location", PRESENT_JOURNAL_REDIRECT_URI + templateId)
          .build();
    }
    return new ResponseEntity<>(createdJournal, HttpStatus.CREATED);
  }

  @Operation(description = "Updates template journal")
  @PutMapping("templates/journal/{trackedTemplateId}")
  public void updateTrackedTemplate(@Valid @RequestBody final UpdateTrackedTemplateRequest request,
      @NotBlank @PathVariable final String trackedTemplateId) {
    trackedTemplateService.updateTrackedTemplate(request, trackedTemplateId);
  }

  @Operation(description = "Get journal of complete template created by a particular user")
  @GetMapping("templates/{templateId}/journal")
  public TrackedTemplate getTrackedTemplateForUser(@NotBlank @PathVariable final String templateId,
      @NotBlank @RequestParam final String ownerName) {
    return trackedTemplateService.getJournalFromTemplateForOwner(templateId, ownerName);
  }

  @Operation(description = "Get all journals belonging to a given user")
  @GetMapping("users/{username}/templates/journal")
  public List<TrackedTemplate> getAllTrackedTemplatesForUser(@NotBlank @PathVariable final String username) {
    return trackedTemplateService.getJournalsForUser(username);
  }

  @Operation(description = "Delete template journal belonging to user")
  @DeleteMapping("/users/{username}/templates/journal/{trackedTemplateId}")
  public boolean deleteTrackedTemplate(@NotBlank @PathVariable String username,
      @NotBlank @PathVariable String trackedTemplateId) {
    return trackedTemplateService.deleteTrackedTemplate(username, trackedTemplateId);
  }

}
