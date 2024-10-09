package com.test.security.users.model.dto;

import com.test.user.models.CreateTemplateUserRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @Valid
    private UserDetailsRequest authorizationRequest;
    @Valid
    private CreateTemplateUserRequest createTemplateUserRequest;

}
