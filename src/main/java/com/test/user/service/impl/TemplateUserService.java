package com.test.user.service.impl;

import com.test.security.jwt.JwtUtil;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TemplateUserService {

  private static final String USER_NOT_FOUND = "Unable to find user with id %s";
  private static final String USERNAME_NOT_FOUND = "Unable to find user with username %s";

  private final TemplateUserCustomRepository templateUserCustomRepository;
  private final TemplateUserRepository templateUserRepository;
  private final FriendRequestRepository friendRequestRepository;
  private final TemplateUserMapper templateUserMapper;

  
  public TemplateUser createUser(final CreateTemplateUserRequest request) {
    TemplateUser user = TemplateUser.builder()
        //.username(request.getUsername())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .friends(new ArrayList<>())
        .groups(new HashSet<>())
        .currentEmail(request.getEmail().toLowerCase())
        .country(request.getCountry())
        .region(request.getRegion())
        .joinDate(LocalDateTime.now())
        .build();
    return templateUserRepository.save(user);
  }

  
  public void registerUser(String username, CreateTemplateUserRequest request) {
    TemplateUser user = TemplateUser.builder()
        .username(username)
        .firstName(StringUtils.capitalize(request.getFirstName()))
        .lastName(StringUtils.capitalize(request.getLastName()))
        .friends(new ArrayList<>())
        .groups(new HashSet<>())
        .currentEmail(request.getEmail().toLowerCase())
        .previousEmails(new ArrayList<>())
        .likedTemplates(new HashMap<>())
        .country(request.getCountry())
        .region(request.getRegion())
        .joinDate(LocalDateTime.now())
        .build();
    templateUserRepository.insert(user);
  }

  
  public TemplateUser getUserByUsername(String username) {
    return templateUserRepository.getTemplateUserByUsername(username)
        .orElseThrow(() -> new TemplateUserException(String.format(USERNAME_NOT_FOUND, username), HttpStatus.NOT_FOUND));
  }

  public List<TemplateUserResponse> searchUsers(String search) {
    return templateUserMapper.toPublicResponseList(templateUserCustomRepository.searchUsers(search));
  }

  public List<TemplateUserResponse> getRecentUsers(Integer count) {
    return templateUserMapper.toPublicResponseList(templateUserCustomRepository.findRecentUsers(count));
  }

  public void updateTemplateUser(String username, UpdateTemplateUserRequest request) {
    TemplateUser user = templateUserRepository.getTemplateUserByUsername(username)
        .orElseThrow(() -> new TemplateUserException(String.format(USERNAME_NOT_FOUND, username), HttpStatus.BAD_REQUEST));

    if (StringUtils.isNotBlank(request.getFirstName())) {
      user.setFirstName(request.getFirstName());
    }

    if (StringUtils.isNotBlank(request.getLastName())) {
      user.setLastName(request.getLastName());
    }

    if (StringUtils.isNotBlank(request.getCountry())) {
      user.setCountry(request.getCountry());
    }

    if (StringUtils.isNotBlank(request.getRegion())) {
      user.setRegion(request.getRegion());
    }

    if (StringUtils.isNotBlank(request.getProfileImageUrl())) {
      user.setProfileImageUrl(request.getProfileImageUrl());
    }

    if (StringUtils.isNotBlank(request.getNewEmail())) {
      user.getPreviousEmails().add(user.getCurrentEmail());
      user.setCurrentEmail(request.getNewEmail());
    }

    user.setLastUpdatedAt(LocalDateTime.now());
    templateUserRepository.save(user);
  }

  
  public FriendRequest sendFriendRequest(final String recipient) {
    List<String> recipientFriends = fetchUser(recipient).getFriends();
    String sender = Objects.requireNonNull(JwtUtil.getLoggedInUser());

    if (recipientFriends.contains(sender)) {
      throw new TemplateUserException("Sender and recipient are already friends", HttpStatus.BAD_REQUEST);
    }

    if (friendRequestRepository.existsBySenderAndRecipient(sender, recipient)) {
      throw new TemplateUserException("Friend request has already been sent", HttpStatus.BAD_REQUEST);
    }

    if (friendRequestRepository.existsBySenderAndRecipient(recipient, sender)) {
      throw new TemplateUserException("Cannot send invite as there is already a pending request", HttpStatus.BAD_REQUEST);
    }

    FriendRequest request = FriendRequest.builder()
        .sender(sender)
        .recipient(recipient)
        .sendDate(Date.from(Instant.now()))
        .build();
    return friendRequestRepository.insert(request);
  }

  
  public void acceptRequest(final String sender) {
    //Logged in user is the recipient, i.e. the one who can accept/reject
    String loggedInUser = JwtUtil.getLoggedInUser();

    FriendRequest request = friendRequestRepository.findBySenderAndRecipient(sender, loggedInUser)
        .orElseThrow(() -> new TemplateUserException("Friend request does not exist", HttpStatus.BAD_REQUEST));
    if (!request.getRecipient().equals(Objects.requireNonNull(loggedInUser))) {
      throw new TemplateUserException("Recipient does not match logged in user", HttpStatus.BAD_REQUEST);
    }

    templateUserRepository.pushUserToFriendsList(request.getSender(), request.getRecipient());
    templateUserRepository.pushUserToFriendsList(request.getRecipient(), request.getSender());
    friendRequestRepository.deleteBySenderAndRecipient(sender, loggedInUser);
  }

  public List<TemplateUserResponse> getUserFriends() {
    String loggedInUser = JwtUtil.getLoggedInUser();

    TemplateUser user = fetchUser(loggedInUser);
    List<TemplateUser> userFriends = templateUserCustomRepository.findAllUsersInList(user.getFriends());

    return templateUserMapper.toPublicResponseList(userFriends);
  }

  
  public List<FriendRequest> getSentRequestsForUser() {
    String user = JwtUtil.getLoggedInUser();
    return friendRequestRepository.findAllBySender(user);
  }
  
  public List<FriendRequest> getPendingRequestsForUser() {
    String user = JwtUtil.getLoggedInUser();
    return friendRequestRepository.findAllByRecipient(user);
  }
  
  public void removeFriend(String friendName) {
    String user = JwtUtil.getLoggedInUser();

    TemplateUser retrievedUser = fetchUser(user);
    TemplateUser friendToDelete = fetchUser(friendName);

    friendToDelete.getFriends().remove(user);
    retrievedUser.getFriends().remove(friendName);

    templateUserRepository.saveAll(List.of(friendToDelete, retrievedUser));
  }

  
  public void declineFriendRequest(final String sender) {
    //Logged in user is recipient
    String loggedInUser = JwtUtil.getLoggedInUser();

    FriendRequest request = friendRequestRepository.findBySenderAndRecipient(sender, loggedInUser)
        .orElseThrow(() -> new TemplateUserException(String.format("Request %s does not exist", sender), HttpStatus.BAD_REQUEST));

    friendRequestRepository.delete(request);
  }

  
  public void deleteFriendRequest(final String requestId) {
    friendRequestRepository.deleteById(requestId);
  }

  private TemplateUser fetchUser(String username) {
    return templateUserRepository.getTemplateUserByUsername(username)
        .orElseThrow(() -> new TemplateUserException(String.format(USERNAME_NOT_FOUND, username), HttpStatus.BAD_REQUEST));
  }

}
