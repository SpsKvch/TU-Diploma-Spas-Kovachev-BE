package com.test.user.models.groups;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupRequest {

    @NotBlank(message = "Group name cannot be blank")
    private String name;
    @NotBlank(message = "A group must have a description")
    private String description;

}
