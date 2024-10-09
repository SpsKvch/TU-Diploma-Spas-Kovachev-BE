package com.test.template.validation;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AccessValidator {

  private static final String PUBLIC_ACCESS_MISMATCH = "Public templates must have ALL branch permission";
  private static final String ACCESS_MISMATCH = "Branch permission %s is not applicable to template with %s access status";
  private static final String PERSONAL_STATUS_MISMATCH = "Status must be kept personal in accordance with the parent";
  private static final String PRIVATE_STATUS_MISMATCH = "Status must either be PERSONAL or PRIVATE";
  private static final String GROUP_STATUS_MISMATCH = "Drafts branching from template with GROUP status cannot be PUBLIC";

  public void validateStatuses(final AccessStatus status, final BranchPermission permission) {
    if (status.equals(AccessStatus.PUBLIC) && permission != BranchPermission.ALL) {
      throw new TemplateException(PUBLIC_ACCESS_MISMATCH, HttpStatus.BAD_REQUEST);
    }

    if (status.equals(AccessStatus.PRIVATE) && !permission.applicableToPrivateAccess()) {
      throw new TemplateException(String.format(ACCESS_MISMATCH, permission, status), HttpStatus.BAD_REQUEST);
    }

    if (status.equals(AccessStatus.PERSONAL) && !permission.applicableToPersonalAccess()) {
      throw new TemplateException(String.format(ACCESS_MISMATCH, permission, status), HttpStatus.BAD_REQUEST);
    }
  }

  public void validateStatusInRelationToParent(final AccessStatus draftStatus, final AccessStatus parentStatus) {
    if (parentStatus.equals(AccessStatus.PERSONAL) && !draftStatus.equals(AccessStatus.PERSONAL)) {
      throw new TemplateException(PERSONAL_STATUS_MISMATCH, HttpStatus.BAD_REQUEST);
    }

    if (parentStatus.equals(AccessStatus.PRIVATE) && !draftStatus.isPrivateOrPersonal()) {
      throw new TemplateException(PRIVATE_STATUS_MISMATCH, HttpStatus.BAD_REQUEST);
    }

    if (parentStatus.equals(AccessStatus.GROUP) && draftStatus.equals(AccessStatus.PUBLIC)) {
      throw new TemplateException(GROUP_STATUS_MISMATCH, HttpStatus.BAD_REQUEST);
    }
  }

}
