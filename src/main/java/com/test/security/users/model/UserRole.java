package com.test.security.users.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class UserRole implements GrantedAuthority {

  private String authority;

  public UserRole of(String authority) {
    this.authority = authority;
    return this;
  }

}
