package com.test.user.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserResponse {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String refreshToken;
    private LocalDateTime joinDate;

}
