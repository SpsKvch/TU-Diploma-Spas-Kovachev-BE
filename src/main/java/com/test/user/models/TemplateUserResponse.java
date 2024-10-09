package com.test.user.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUserResponse {

  private String username;
  private String profileImageUrl;
  private List<String> friends;
  private Set<String> groups;
  private Map<String, Boolean> likedTemplates;
  private String country;
  private LocalDateTime joinDate;

}
