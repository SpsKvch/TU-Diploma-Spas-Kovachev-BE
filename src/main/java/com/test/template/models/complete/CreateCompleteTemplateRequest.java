package com.test.template.models.complete;

import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static com.test.template.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompleteTemplateRequest {

    @NotNull(message = NO_CATEGORY_ERROR_MESSAGE)
    private String categoryId;
    @NotEmpty(message = "At least one tag must be selected!")
    private Set<String> tags = new HashSet<>();
    @NotNull(message = NO_ACCESS_STATUS_ERROR_MESSAGE)
    private AccessStatus accessStatus;
    @NotNull(message = NO_BRANCH_PERMISSION_ERROR_MESSAGE)
    private BranchPermission branchPermission;
    private Set<String> sharedWith;

}
