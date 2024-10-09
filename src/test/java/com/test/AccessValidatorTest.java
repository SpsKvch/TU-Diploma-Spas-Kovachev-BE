package com.test;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import com.test.template.validation.AccessValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.test.template.models.enums.AccessStatus.*;
import static com.test.template.models.enums.BranchPermission.ALL;
import static com.test.template.models.enums.BranchPermission.GROUP_ONLY;

@ExtendWith(SpringExtension.class)
public class AccessValidatorTest {

    private static final String PUBLIC_ACCESS_MISMATCH = "Public templates must have ALL branch permission";
    private static final String ACCESS_MISMATCH = "Branch permission %s is not applicable to template with %s access status";
    private static final String PERSONAL_STATUS_MISMATCH = "Status must be kept personal in accordance with the parent";
    private static final String PRIVATE_STATUS_MISMATCH = "Status must either be PERSONAL or PRIVATE";
    private static final String GROUP_STATUS_MISMATCH = "Drafts branching from template with GROUP status cannot be PUBLIC";

    private final AccessValidator accessValidator = new AccessValidator();

    @Test
    public void validateStatusesException() {
        assertStatusesException(PUBLIC, GROUP_ONLY, PUBLIC_ACCESS_MISMATCH);
        assertStatusesException(PRIVATE, ALL, String.format(ACCESS_MISMATCH, PRIVATE, ALL));
        assertStatusesException(PERSONAL, GROUP_ONLY, String.format(ACCESS_MISMATCH, PERSONAL, GROUP_ONLY));
    }

    private void assertStatusesException(AccessStatus accessStatus, BranchPermission permission, String msg) {
        Assertions.assertThrows(TemplateException.class,
                () -> accessValidator.validateStatuses(accessStatus, permission), msg);
    }

    @Test
    public void validateStatusInRelationToParentException() {
       assertParentStatusesException(PUBLIC, PERSONAL, PERSONAL_STATUS_MISMATCH);
       assertParentStatusesException(PUBLIC, PRIVATE, PRIVATE_STATUS_MISMATCH);
       assertParentStatusesException(PUBLIC, GROUP, GROUP_STATUS_MISMATCH);
    }

    private void assertParentStatusesException(AccessStatus accessStatus, AccessStatus parentStatus, String msg) {
        Assertions.assertThrows(TemplateException.class,
                () -> accessValidator.validateStatusInRelationToParent(accessStatus, parentStatus), msg);
    }

}
