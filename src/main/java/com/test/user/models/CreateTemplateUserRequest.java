package com.test.user.models;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.Duration;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateUserRequest {

    private String firstName;
    private String lastName;
    @Email(message = "Email is malformed")
    private String email;
    private String country;
    private String region;
}
