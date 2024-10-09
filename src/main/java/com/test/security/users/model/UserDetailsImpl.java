package com.test.security.users.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("userDetails")
public class UserDetailsImpl implements UserDetails {

    public UserDetailsImpl(String username, String password) {
        this.username = username;
        this.password = password;
        this.authorities = new HashSet<>();
    }

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;
    private Set<UserRole> authorities;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

}
