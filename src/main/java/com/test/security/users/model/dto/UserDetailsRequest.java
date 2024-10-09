package com.test.security.users.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsRequest {

    @NotBlank(message = "Username is a required field")
    private String username;
    @NotBlank
    private String password;

}
