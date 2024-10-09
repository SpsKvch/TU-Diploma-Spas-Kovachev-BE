package com.test.user.service.impl;

import com.test.user.exception.TemplateUserException;
import com.test.user.models.groups.UserGroup;
import com.test.user.models.groups.UserGroupRequest;
import com.test.user.repository.TemplateUserRepository;
import com.test.user.repository.UserGroupRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import static com.test.utils.ObjectsUtil.ID;
import static com.test.utils.ObjectsUtil.SECOND_USER;
import static com.test.utils.ObjectsUtil.USER;
import static com.test.utils.ObjectsUtil.createUserGroup;
import static com.test.utils.ObjectsUtil.createUserGroupRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceTest {

  private Authentication authentication = Mockito.mock(Authentication.class);
  private SecurityContext securityContext = Mockito.mock(SecurityContext.class);
  @Mock
  private TemplateUserRepository templateUserRepository;
  @Mock
  private UserGroupRepository groupRepository;
  @InjectMocks
  private UserGroupService userGroupService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createUserGroup_ValidRequest_GroupCreated() {
    UserGroupRequest request = createUserGroupRequest();
    UserGroup group = createUserGroup();
    group.setId(ID);

    mockSecurity(USER);
    when(groupRepository.save(any())).thenReturn(group);

    var result = userGroupService.createUserGroup(request);

    verify(templateUserRepository, times(1)).addGroupToUser(USER, ID);
    assertEquals(group, result);
  }

  @Test
  void updateUserGroup_AnyArguments_GroupUpdated() {
    var request = createUserGroupRequest();

    userGroupService.updateUserGroup(ID, request);

    verify(groupRepository, times(1)).updateUserGroup(ID, request.getName(), request.getDescription());
  }

  @Test
  void joinGroup_UserNotPartOfGroup_UserAddedToGroup() {
    UserGroup userGroup = createUserGroup();
    when(groupRepository.findById(ID)).thenReturn(Optional.of(userGroup));

    mockSecurity(SECOND_USER);

    userGroupService.joinGroup(ID);

    verify(groupRepository, times(1)).save(userGroup);
    assertTrue(userGroup.getMembers().contains(SECOND_USER));
  }

  @Test
  void joinGroup_UserAlreadyInGroup_Exception() {
    UserGroup userGroup = createUserGroup();
    when(groupRepository.findById(ID)).thenReturn(Optional.of(userGroup));

    mockSecurity(USER);

    assertThrows(TemplateUserException.class, () -> userGroupService.joinGroup(ID));
  }

  @Test
  void joinGroup_GroupDoesNotExist_Exception() {
    when(groupRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> userGroupService.joinGroup(ID));
  }

  @Test
  void getUserGroupById_GroupExists_GroupReturned() {
    UserGroup userGroup = createUserGroup();

    when(groupRepository.findById(ID)).thenReturn(Optional.of(userGroup));

    var result = userGroupService.getUserGroupById(ID);

    assertEquals(userGroup, result);
  }

  @Test
  void getUserGroupById_GroupNotExists_Exception() {
    when(groupRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(TemplateUserException.class, () -> userGroupService.getUserGroupById(ID));
  }

  @Test
  void leaveGroup_UserInGroup_UserRemovedFromGroup() {
    UserGroup userGroup = createUserGroup();
    userGroup.getMembers().add(SECOND_USER);

    mockSecurity(SECOND_USER);
    when(groupRepository.findById(ID)).thenReturn(Optional.of(userGroup));

    userGroupService.leaveGroup(ID);

    verify(templateUserRepository, times(1)).pullGroupFromUser(SECOND_USER, ID);
    verify(groupRepository, times(1)).save(userGroup);
    assertFalse(userGroup.getMembers().contains(SECOND_USER));
  }

  @Test
  void leaveGroup_UserCreatedGroup_Exception() {
    UserGroup userGroup = createUserGroup();

    mockSecurity(USER);
    when(groupRepository.findById(ID)).thenReturn(Optional.of(userGroup));

    assertThrows(TemplateUserException.class, () -> userGroupService.leaveGroup(ID));
  }

  private void mockSecurity(String username) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }

}