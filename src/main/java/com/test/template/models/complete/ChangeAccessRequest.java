package com.test.template.models.complete;

import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAccessRequest {

    public AccessStatus accessStatus;
    public BranchPermission branchPermission;

}
