package com.test.user.controller;

import com.test.user.models.CreateTemplateUserRequest;
import com.test.user.models.FriendRequest;
import com.test.user.models.TemplateUser;
import com.test.user.models.TemplateUserResponse;
import com.test.user.models.UpdateTemplateUserRequest;
import com.test.user.service.impl.TemplateUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1")
public class TemplateUserController {

  private final TemplateUserService templateUserService;

  @PostMapping("/users")
  public ResponseEntity<TemplateUser> createUser(@Valid @RequestBody CreateTemplateUserRequest request) {
    return new ResponseEntity<>(templateUserService.createUser(request), HttpStatus.CREATED);
  }

  @GetMapping("/users/{username}")
  public ResponseEntity<TemplateUser> getTemplateUserByUsername(@Valid @PathVariable String username) {
    return new ResponseEntity<>(templateUserService.getUserByUsername(username), HttpStatus.OK);
  }

  @GetMapping("/users/search")
  public ResponseEntity<List<TemplateUserResponse>> getRecentTemplateUsers(@RequestParam String search) {
    return new ResponseEntity<>(templateUserService.searchUsers(search), HttpStatus.OK);
  }

  @GetMapping("/users")
  public ResponseEntity<List<TemplateUserResponse>> getRecentTemplateUsers(@RequestParam(required = false) Integer count) {
    return new ResponseEntity<>(templateUserService.getRecentUsers(count), HttpStatus.OK);
  }

  @PatchMapping("/users/{username}")
  public ResponseEntity<Void> updateUser(@NotBlank @PathVariable String username, @RequestBody UpdateTemplateUserRequest request) {
    templateUserService.updateTemplateUser(username, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/users/friends/requests")
  public ResponseEntity<FriendRequest> sendFriendRequest(@NotBlank @RequestParam String recipient) {
    return ResponseEntity.ok(templateUserService.sendFriendRequest(recipient));
  }

  @PostMapping("/users/friends/requests/{sender}/accept")
  public void acceptFriendRequest(@NotBlank @PathVariable String sender) {
    templateUserService.acceptRequest(sender);
  }

  @GetMapping("/users/friends")
  public ResponseEntity<List<TemplateUserResponse>> getUserFriends() {
    return ResponseEntity.ok(templateUserService.getUserFriends());
  }

  @GetMapping("/users/friends/requests/sent")
  public ResponseEntity<List<FriendRequest>> getSentFriendRequests() {
    return ResponseEntity.ok(templateUserService.getSentRequestsForUser());
  }

  @GetMapping("/users/friends/requests/pending")
  public ResponseEntity<List<FriendRequest>> getPendingFriendRequests() {
    return ResponseEntity.ok(templateUserService.getPendingRequestsForUser());
  }

  @DeleteMapping("/users/friends/{friendName}")
  public void removeFriend(@NotBlank @PathVariable String friendName) {
    templateUserService.removeFriend(friendName);
  }

  @DeleteMapping("/users/friends/requests/{sender}/decline")
  public void declineFriendRequest(@NotBlank @PathVariable String sender) {
    templateUserService.declineFriendRequest(sender);
  }

  @DeleteMapping("/users/friends/requests/{requestId}")
  public void deleteFriendRequest(@NotBlank @PathVariable String requestId) {
    templateUserService.deleteFriendRequest(requestId);
  }

}
