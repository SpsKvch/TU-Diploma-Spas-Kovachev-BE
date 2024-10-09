package com.test.security.users.service.impl;

import com.test.security.exceptions.JwtAuthenticationException;
import com.test.security.jwt.JwtUtil;
import com.test.security.users.model.UserDetailsImpl;
import com.test.security.users.model.UserRole;
import com.test.security.users.model.dto.RefreshTokenRequest;
import com.test.security.users.model.dto.RegistrationRequest;
import com.test.security.users.model.dto.UserDetailsRequest;
import com.test.security.users.repository.UserDetailsRepository;
import com.test.security.users.service.IUserLoginService;
import com.test.user.mapper.TemplateUserMapper;
import com.test.user.models.CreateTemplateUserRequest;
import com.test.user.models.SimpleUserResponse;
import com.test.user.service.impl.TemplateUserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.CharBuffer;


import static com.test.utils.Constants.ROOT;

@Slf4j
@Component
public class UserLoginService implements IUserLoginService {

  private static final String ADMIN = "admin";

  private final TemplateUserService templateUserService;
  private final UserDetailsRepository userDetailsRepository;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final TemplateUserMapper userMapper;

  public UserLoginService(TemplateUserService templateUserService,
      UserDetailsRepository userDetailsRepository,
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder,
      TemplateUserMapper userMapper) {
    this.templateUserService = templateUserService;
    this.userDetailsRepository = userDetailsRepository;
    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  @Override
  public SimpleUserResponse loginUser(UserDetailsRequest request) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
    Authentication authentication = authenticationManager.authenticate(token);

    String username = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
    log.info("Logged in user: {}", username);
    SimpleUserResponse response = userMapper.toLoginResponse(templateUserService.getUserByUsername(username));
    response.setRefreshToken(JwtUtil.generateRefreshToken());
    return response;
  }

  @Override
  //@Transactional
  public SimpleUserResponse registerUser(final RegistrationRequest registrationRequest) {

    UserDetailsRequest auth = registrationRequest.getAuthorizationRequest();
    String encodedPassword = passwordEncoder.encode(CharBuffer.wrap(auth.getPassword()));

    UserDetailsImpl userDetails = new UserDetailsImpl(auth.getUsername(), encodedPassword);

    userDetails = userDetailsRepository.insert(userDetails);

    CreateTemplateUserRequest createUser = registrationRequest.getCreateTemplateUserRequest();
    templateUserService.registerUser(userDetails.getUsername(), createUser);

    return loginUser(auth);
  }

  @Override
  public boolean logoutUser(HttpServletRequest request) {
    try {
      request.logout();
    } catch (ServletException e) {
      log.error("Unable to logout current user.");
      return false;
    }
    return true;
  }

  @Override
  public void authenticateUser(String token) {
    JwtUtil.validateToken(token);
  }

  @Override
  public String refreshToken(RefreshTokenRequest refreshToken) {
    try {
      JwtUtil.validateToken(refreshToken.getRefreshToken());
    } catch (JwtAuthenticationException e) {
      log.info("Refresh token has expired");
      return null;
    }
    return JwtUtil.generateToken(refreshToken.getUsername());
  }

  @Override
  public void grantUserAdminPermission(String username) {
    Optional<UserDetails> userDetails = userDetailsRepository.getUserDetailsImplByUsername(username);
    if (userDetails.isPresent()) {
      UserDetailsImpl userDetailsEntity = (UserDetailsImpl) userDetails.get();
      UserRole adminRole = new UserRole().of(ADMIN);
      if (userDetailsEntity.getAuthorities().add(adminRole)) {
        userDetailsRepository.save(userDetailsEntity);
      }
    }
  }

  @Override
  public List<String> getUserPermissions() {
    String username = JwtUtil.getLoggedInUser();
    UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsRepository.getUserDetailsImplByUsername(username).orElseThrow(
        () -> new JwtAuthenticationException("Error parsing JWT token!"));
    return userDetails.getAuthorities().stream()
        .map(UserRole::getAuthority)
        .toList();
  }

  @PostConstruct
  private void createRootUserIfNotExists() {
    if (!userDetailsRepository.existsByUsername(ROOT)) {
      //Username, password and functionality kept simple for demonstration purposes
      String encodedPassword = passwordEncoder.encode(CharBuffer.wrap(ROOT));
      UserDetailsImpl userDetails = new UserDetailsImpl(ROOT, encodedPassword);
      userDetails.getAuthorities().add(new UserRole().of(ROOT));

      userDetailsRepository.save(userDetails);
      templateUserService.registerUser(ROOT, createDummyTemplateUser());
    }
  }

  private CreateTemplateUserRequest createDummyTemplateUser() {
    CreateTemplateUserRequest request = new CreateTemplateUserRequest();
    request.setEmail("dummy@mail.mail");
    request.setCountry("dummy");
    request.setRegion("dummy");
    return request;
  }

}
