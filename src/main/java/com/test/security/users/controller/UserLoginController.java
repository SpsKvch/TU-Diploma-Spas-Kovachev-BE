package com.test.security.users.controller;

import com.test.security.jwt.JwtUtil;
import com.test.security.users.model.dto.RefreshTokenRequest;
import com.test.security.users.model.dto.RegistrationRequest;
import com.test.security.users.model.dto.UserDetailsRequest;
import com.test.security.users.service.IUserLoginService;
import com.test.user.models.SimpleUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserLoginController {

  private final IUserLoginService userLoginService;

  @PostMapping("/v1/users/login")
  public ResponseEntity<SimpleUserResponse> login(@Valid @RequestBody UserDetailsRequest request) {
    SimpleUserResponse user = userLoginService.loginUser(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, JwtUtil.generateToken(user.getUsername()))
        .body(user);
  }

  @PostMapping("/v1/users/register")
  public ResponseEntity<SimpleUserResponse> registerUser(@Valid @RequestBody RegistrationRequest request) {
    SimpleUserResponse createdUser = userLoginService.registerUser(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, JwtUtil.generateToken(createdUser.getUsername()))
        .body(createdUser);
  }

  @PostMapping("/v1/users/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    boolean userLoggedOut = userLoginService.logoutUser(request);
    if (!userLoggedOut) {
      return ResponseEntity.internalServerError().build();
    }
    return ResponseEntity.ok().build();
  }

  @PostMapping("/v1/auth")
  public ResponseEntity<Void> authenticate(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
    userLoginService.authenticateUser(auth.split(" ")[1]);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/v1/auth/refresh")
  public ResponseEntity<Boolean> refreshToken(@RequestBody RefreshTokenRequest request) {
    String token = userLoginService.refreshToken(request);

    boolean tokenGenerated = token != null;

    HttpHeaders headers = new HttpHeaders();
    if (tokenGenerated) {
      headers.add(HttpHeaders.AUTHORIZATION, token);
    }

    return ResponseEntity.ok()
        .headers(headers)
        .body(tokenGenerated);
  }

  @PatchMapping("/v1/users/{username}/grant/admin")
  public ResponseEntity<Void> grantUserAdminPermission(@PathVariable String username) {
    userLoginService.grantUserAdminPermission(username);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/v1/users/permissions")
  public ResponseEntity<List<String>> getUserPermissions() {
    return ResponseEntity.ok(userLoginService.getUserPermissions());
  }

}
