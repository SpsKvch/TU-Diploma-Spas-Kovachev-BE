package com.test.user.service.impl;

import com.test.user.exception.TemplateUserException;
import com.test.user.mapper.TemplateUserMapper;
import com.test.user.models.CreateTemplateUserRequest;
import com.test.user.models.FriendRequest;
import com.test.user.models.TemplateUser;
import com.test.user.models.TemplateUserResponse;
import com.test.user.models.UpdateTemplateUserRequest;
import com.test.user.repository.FriendRequestRepository;
import com.test.user.repository.TemplateUserCustomRepository;
import com.test.user.repository.TemplateUserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import static com.test.utils.ObjectsUtil.EMAIL;
import static com.test.utils.ObjectsUtil.SECOND_USER;
import static com.test.utils.ObjectsUtil.USER;
import static com.test.utils.ObjectsUtil.createCreateTemplateUserRequest;
import static com.test.utils.ObjectsUtil.createFriendRequest;
import static com.test.utils.ObjectsUtil.createTemplateFilters;
import static com.test.utils.ObjectsUtil.createTemplateUserResponse;
import static com.test.utils.ObjectsUtil.createUpdateTemplateUserRequest;
import static com.test.utils.ObjectsUtil.createUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TemplateUserServiceTest {

  private static final String PREV_EMAIL = "previous";
  private static final String SENDER_HAS_PENDING_REQUEST_MESSAGE = "Cannot send invite as there is already a pending request";
  private static final String SENDER_HAS_SENT_REQUEST_MESSAGE = "Friend request has already been sent";
  private static final String ALREADY_FRIENDS_MESSAGE = "Sender and recipient are already friends";

  private Authentication authentication = Mockito.mock(Authentication.class);
  private SecurityContext securityContext = Mockito.mock(SecurityContext.class);

  @Mock
  private TemplateUserCustomRepository templateUserCustomRepository;
  @Mock
  private TemplateUserRepository templateUserRepository;
  @Mock
  private FriendRequestRepository friendRequestRepository;
  @Mock
  private TemplateUserMapper templateUserMapper;
  @InjectMocks
  private TemplateUserService templateUserService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createUser_ValidRequest_UserCreated() {
    CreateTemplateUserRequest request = createCreateTemplateUserRequest();
    TemplateUser expectedUser = createUser();

    when(templateUserRepository.save(any())).thenReturn(expectedUser);

    var result = templateUserService.createUser(request);

    assertEquals(expectedUser, result);
  }

  @Test
  void registerUser_ValidRequest_UserCreated() {
    CreateTemplateUserRequest request = createCreateTemplateUserRequest();

    templateUserService.registerUser(USER, request);

    verify(templateUserRepository).insert(any(TemplateUser.class));
  }

  @Test
  void getUserByUsername_ExistingUser_ReturnsUser() {
    TemplateUser user = createUser();

    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));

    var result = templateUserService.getUserByUsername(USER);

    assertEquals(user, result);
  }

  @Test
  void getUserByUsername_NotExistingUser_Exception() {
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> templateUserService.getUserByUsername(USER));
  }

  @Test
  void searchUsers_UsersFound_UsersReturned() {
    List<TemplateUser> users = List.of(createUser());
    List<TemplateUserResponse> response = List.of(createTemplateUserResponse());

    when(templateUserCustomRepository.searchUsers(USER)).thenReturn(users);
    when(templateUserMapper.toPublicResponseList(users)).thenReturn(response);

    var result = templateUserService.searchUsers(USER);

    assertEquals(response, result);
  }

  @Test
  void getRecentUsers_UsersFound_UsersReturned() {
    List<TemplateUser> users = List.of(createUser());
    List<TemplateUserResponse> response = List.of(createTemplateUserResponse());

    when(templateUserCustomRepository.findRecentUsers(1)).thenReturn(users);
    when(templateUserMapper.toPublicResponseList(users)).thenReturn(response);

    var result = templateUserService.getRecentUsers(1);

    assertEquals(response, result);
  }

  @Test
  void updateTemplateUser_AllParametersProvided_UserUpdated() {
    LocalDateTime now = LocalDateTime.now();

    UpdateTemplateUserRequest request = createUpdateTemplateUserRequest();
    TemplateUser user = new TemplateUser();
    user.setUsername(USER);
    user.setCurrentEmail(PREV_EMAIL);

    TemplateUser expectedUser = createUser();
    expectedUser.setId(null);
    expectedUser.setFriends(new ArrayList<>());
    expectedUser.setPreviousEmails(List.of(PREV_EMAIL));
    expectedUser.setLastUpdatedAt(now);

    MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class);
    mockedStatic.when(LocalDateTime::now).thenReturn(now);

    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));

    templateUserService.updateTemplateUser(USER, request);

    verify(templateUserRepository, times(1)).save(user);
    assertEquals(expectedUser, user);

    mockedStatic.close();
  }

  @Test
  void updateTemplateUser_NoUserFound_Exception() {
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> templateUserService.updateTemplateUser(USER, new UpdateTemplateUserRequest()));

    verify(templateUserRepository, never()).save(any());
  }

  @Test
  void sendFriendRequest_ValidSenderNotFriend_RequestSaved() {
    Date date = Date.from(Instant.now());

    TemplateUser recipient = createUser();
    recipient.setFriends(new ArrayList<>());

    FriendRequest expectedFriendRequest = createFriendRequest();
    expectedFriendRequest.setSendDate(date);

    MockedStatic<Date> mockedStatic = mockStatic(Date.class);
    mockedStatic.when(() -> Date.from(any())).thenReturn(date);

    mockSecurity(SECOND_USER);
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(recipient));
    when(friendRequestRepository.existsBySenderAndRecipient(SECOND_USER, USER)).thenReturn(false);
    when(friendRequestRepository.existsBySenderAndRecipient(USER, SECOND_USER)).thenReturn(false);
    when(friendRequestRepository.insert(expectedFriendRequest)).thenReturn(expectedFriendRequest);

    var result = templateUserService.sendFriendRequest(USER);

    assertEquals(expectedFriendRequest, result);

    mockedStatic.close();
  }

  @Test
  void sendFriendRequest_RecipientHasSentToSender_Exception() {
    TemplateUser recipient = createUser();
    recipient.setFriends(new ArrayList<>());

    mockSecurity(SECOND_USER);
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(recipient));
    when(friendRequestRepository.existsBySenderAndRecipient(SECOND_USER, USER)).thenReturn(false);
    when(friendRequestRepository.existsBySenderAndRecipient(USER, SECOND_USER)).thenReturn(true);

    var exception = assertThrows(TemplateUserException.class, () -> templateUserService.sendFriendRequest(USER));

    assertEquals(exception.getMessage(), SENDER_HAS_PENDING_REQUEST_MESSAGE);
    verify(friendRequestRepository, never()).insert(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_RecipientHasPendingRequest_Exception() {
    TemplateUser recipient = createUser();
    recipient.setFriends(new ArrayList<>());

    mockSecurity(SECOND_USER);
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(recipient));
    when(friendRequestRepository.existsBySenderAndRecipient(SECOND_USER, USER)).thenReturn(true);

    var exception = assertThrows(TemplateUserException.class, () -> templateUserService.sendFriendRequest(USER));

    assertEquals(exception.getMessage(), SENDER_HAS_SENT_REQUEST_MESSAGE);
    verify(friendRequestRepository, never()).insert(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_AlreadyFriends_Exception() {
    TemplateUser recipient = createUser();

    mockSecurity(SECOND_USER);
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(recipient));

    var exception = assertThrows(TemplateUserException.class, () -> templateUserService.sendFriendRequest(USER));

    assertEquals(exception.getMessage(), ALREADY_FRIENDS_MESSAGE);
    verify(friendRequestRepository, never()).insert(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_RecipientDoesNotExist_Exception() {
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> templateUserService.sendFriendRequest(USER));
    verify(friendRequestRepository, never()).insert(any(FriendRequest.class));
  }

  @Test
  void acceptRequest_ValidRequest_RequestProcessed() {
    var request = createFriendRequest();

    mockSecurity(USER);
    when(friendRequestRepository.findBySenderAndRecipient(SECOND_USER, USER)).thenReturn(Optional.of(request));

    templateUserService.acceptRequest(SECOND_USER);

    verify(templateUserRepository, times(1)).pushUserToFriendsList(request.getSender(), request.getRecipient());
    verify(templateUserRepository, times(1)).pushUserToFriendsList(request.getRecipient(), request.getSender());
    verify(friendRequestRepository, timeout(1)).deleteBySenderAndRecipient(request.getSender(), request.getRecipient());
  }

  @Test
  void acceptRequest_SenderIsRecipient_Exception() {
    var request = createFriendRequest();

    mockSecurity(SECOND_USER);
    when(friendRequestRepository.findBySenderAndRecipient(SECOND_USER, USER)).thenReturn(Optional.of(request));

    assertThrows(TemplateUserException.class, () -> templateUserService.acceptRequest(SECOND_USER));

    verify(templateUserRepository, never()).pushUserToFriendsList(request.getSender(), request.getRecipient());
    verify(templateUserRepository, never()).pushUserToFriendsList(request.getRecipient(), request.getSender());
    verify(friendRequestRepository, never()).deleteBySenderAndRecipient(request.getSender(), request.getRecipient());
  }

  @Test
  void acceptRequest_RequestNotFound_Exception() {
    mockSecurity(SECOND_USER);
    when(friendRequestRepository.findBySenderAndRecipient(SECOND_USER, USER)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> templateUserService.acceptRequest(SECOND_USER));
  }

  @Test
  void getUserFriends_UserIsLoggedIn_FriendsReturned() {
    TemplateUser user = createUser();
    List<TemplateUser> friends = List.of(new TemplateUser());
    List<TemplateUserResponse> response = List.of(createTemplateUserResponse());

    mockSecurity(USER);
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(user));
    when(templateUserCustomRepository.findAllUsersInList(user.getFriends())).thenReturn(friends);
    when(templateUserMapper.toPublicResponseList(friends)).thenReturn(response);

    var result = templateUserService.getUserFriends();

    assertEquals(response, result);
  }

  @Test
  void getSentRequestsForUser_UserLoggedIn_RequestsReturned() {
    List<FriendRequest> requests = List.of(createFriendRequest());

    mockSecurity(USER);
    when(friendRequestRepository.findAllBySender(USER)).thenReturn(requests);

    var result = templateUserService.getSentRequestsForUser();

    assertEquals(requests, result);
  }

  @Test
  void getPendingRequestsForUser_UserLoggedIn_RequestsReturned() {
    List<FriendRequest> requests = List.of(createFriendRequest());

    mockSecurity(USER);
    when(friendRequestRepository.findAllByRecipient(USER)).thenReturn(requests);

    var result = templateUserService.getPendingRequestsForUser();

    assertEquals(requests, result);
  }

  @Test
  void removeFriend_UserLoggedInAndFriendPresent() {
    TemplateUser loggedInUser = createUser();

    TemplateUser friend = new TemplateUser();
    friend.setUsername(SECOND_USER);
    friend.getFriends().add(USER);

    mockSecurity(USER);
    when(templateUserRepository.getTemplateUserByUsername(USER)).thenReturn(Optional.of(loggedInUser));
    when(templateUserRepository.getTemplateUserByUsername(SECOND_USER)).thenReturn(Optional.of(friend));

    templateUserService.removeFriend(SECOND_USER);

    verify(templateUserRepository, times(1)).saveAll(List.of(friend, loggedInUser));
    assertFalse(loggedInUser.getFriends().contains(SECOND_USER));
    assertFalse(friend.getFriends().contains(USER));
  }

  @Test
  void declineFriendRequest_RequestExists_Declined() {
    FriendRequest request = createFriendRequest();

    mockSecurity(USER);
    when(friendRequestRepository.findBySenderAndRecipient(SECOND_USER, USER)).thenReturn(Optional.of(request));

    templateUserService.declineFriendRequest(SECOND_USER);

    verify(friendRequestRepository, times(1)).delete(request);
  }

  @Test
  void declineFriendRequest_RequestDoesNotExist_Exception() {
    mockSecurity(USER);
    when(friendRequestRepository.findBySenderAndRecipient(SECOND_USER, USER)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> templateUserService.declineFriendRequest(SECOND_USER));
  }

  @Test
  void deleteFriendRequest() {
    templateUserService.deleteFriendRequest("id");

    verify(friendRequestRepository, times(1)).deleteById("id");
  }

  private void mockSecurity(String username) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }

}