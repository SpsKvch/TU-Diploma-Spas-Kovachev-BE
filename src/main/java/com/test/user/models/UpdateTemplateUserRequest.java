package com.test.user.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateUserRequest {

  private String firstName;
  private String lastName;
  private String newEmail;
  private String profileImageUrl;
  private String country;
  private String region;

}
