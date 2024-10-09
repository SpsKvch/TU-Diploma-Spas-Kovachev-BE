package com.test.security.jwt;

import com.test.security.configs.UriConfigurationProperties;
import com.test.security.exceptions.JwtAuthenticationException;
import com.test.security.users.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

  private final UserDetailsServiceImpl userDetailsService;
  private final HandlerExceptionResolver exceptionResolver;
  private final UriConfigurationProperties uriConfigurationProperties;

  public JwtFilter(UserDetailsServiceImpl userDetailsService,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
      UriConfigurationProperties uriConfigurationProperties) {
    this.userDetailsService = userDetailsService;
    this.exceptionResolver = exceptionResolver;
    this.uriConfigurationProperties = uriConfigurationProperties;
  }

  @SuppressWarnings(value = "all")
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = getTokenFromRequest(request);
    if (!authenticateUser(request, token)) {
      exceptionResolver.resolveException(request, response, null, buildInvalidJwtException(token));
      return;
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (Arrays.stream(uriConfigurationProperties.getProtectedGetEndpoints()).anyMatch(uri::matches)) {
      return false;
    }

    boolean uriIsPublic = Arrays.asList(uriConfigurationProperties.getPublicEndpoints()).contains(request.getRequestURI())
        || request.getMethod().equals(HttpMethod.GET.toString());

    //Try to authenticate the user if uri is unprotected as filter will be skipped.
    if (uriIsPublic) {
      String token = getTokenFromRequest(request);
      if (token == null) {
        return true;
      }
      return authenticateUser(request, token);
    }
    //If endpoint is protected continue with normal filter.
    return false;
  }

  private Exception buildInvalidJwtException(String token) {
    String exceptionMessage;
    if (StringUtils.isBlank(token)) {
      exceptionMessage = "No authorization header provided.";
    } else {
      exceptionMessage = String.format("Received authorization header: '%s' is not valid", token);
    }
    return new JwtAuthenticationException(exceptionMessage);
  }

  private boolean authenticateUser(HttpServletRequest request, String token) {
    if (token == null) {
      return false;
    }

    try {
      JwtUtil.validateToken(token);
    } catch (JwtAuthenticationException e) {
      log.warn("Authentication failed for token: {}", token);
      return false;
    }
    userDetailsService.authenticateUser(JwtUtil.extractUsername(token), request);
    return true;
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.isBlank(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.split(" ")[1];
    }
    return null;
  }
}
