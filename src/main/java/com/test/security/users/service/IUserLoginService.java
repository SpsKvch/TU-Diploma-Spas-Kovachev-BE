package com.test.security.users.service;

import com.test.security.users.model.dto.RefreshTokenRequest;
import com.test.security.users.model.dto.RegistrationRequest;
import com.test.security.users.model.dto.UserDetailsRequest;
import com.test.user.models.SimpleUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface IUserLoginService {

  SimpleUserResponse loginUser(final UserDetailsRequest request);

  SimpleUserResponse registerUser(final RegistrationRequest registrationRequest);
  boolean logoutUser(final HttpServletRequest request);

  void authenticateUser(String token);

  String refreshToken(RefreshTokenRequest refreshToken);

  void grantUserAdminPermission(String username);

  List<String> getUserPermissions();
}
