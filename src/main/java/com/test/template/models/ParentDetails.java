package com.test.template.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDetails {

    private String parentId;
    private boolean visibleToOthers;
    private boolean statusPromotable;

}
