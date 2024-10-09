package com.test.security.users.service.impl;

import com.test.security.users.repository.UserDetailsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private static final String UNABLE_TO_FIND_USER = "Unable to find user %s";

  private final UserDetailsRepository userDetailsRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userDetailsRepository.getUserDetailsImplByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(UNABLE_TO_FIND_USER + username));
  }

  public void authenticateUser(String username, HttpServletRequest request) {
    UserDetails userDetails = loadUserByUsername(username);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null,
            Objects.requireNonNull(userDetails).getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(authentication);
  }
}
