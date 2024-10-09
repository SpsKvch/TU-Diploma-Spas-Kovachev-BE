package com.test.user.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUser {

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String firstName;
    private String lastName;
    private String currentEmail;
    private String profileImageUrl;
    private List<String> friends = new ArrayList<>();
    private Set<String> groups = new HashSet<>();
    private List<String> previousEmails = new ArrayList<>();
    private Map<String, Boolean> likedTemplates = new HashMap<>();
    private String country;
    private String region;
    private LocalDateTime joinDate;
    private LocalDateTime lastUpdatedAt;

}
