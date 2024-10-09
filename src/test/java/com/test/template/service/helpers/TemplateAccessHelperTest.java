package com.test.template.service.helpers;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.enums.AccessStatus;
import com.test.template.models.enums.BranchPermission;
import com.test.user.models.TemplateUser;
import com.test.user.models.groups.UserGroup;
import com.test.user.repository.UserGroupRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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


import static com.test.utils.ObjectsUtil.SECOND_USER;
import static com.test.utils.ObjectsUtil.USER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateAccessHelperTest {

  private Authentication authentication = Mockito.mock(Authentication.class);
  private SecurityContext securityContext = Mockito.mock(SecurityContext.class);

  @Mock
  private UserGroupRepository userGroupRepository;
  @InjectMocks
  private TemplateAccessHelper templateAccessHelper;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void templateCanBeAccessed_PersonalTemplateOwner_Success() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(USER);
    template.setAccessStatus(AccessStatus.PERSONAL);

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeAccessed(template);

    assertTrue(result);
  }

  @Test
  void templateCanBeAccessed_PrivateTemplateOwner_Success() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(USER);
    template.setAccessStatus(AccessStatus.PRIVATE);
    template.setSharedWith(new HashSet<>());

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeAccessed(template);

    assertTrue(result);
  }

  @Test
  void templateCanBeAccessed_GroupTemplateUserInGroup_Success() {
    UserGroup userGroup = new UserGroup();
    userGroup.setMembers(Set.of(USER));

    CompleteTemplate template = new CompleteTemplate();
    template.setAccessStatus(AccessStatus.GROUP);
    template.setAssociatedGroup("group");

    mockSecurity(USER);
    when(userGroupRepository.findById(anyString())).thenReturn(Optional.of(userGroup));

    var result = templateAccessHelper.templateCanBeAccessed(template);

    assertTrue(result);
  }

  @Test
  void templateCanBeAccessed_GroupTemplateUserNotInGroup_Exception() {
    CompleteTemplate template = new CompleteTemplate();
    template.setAccessStatus(AccessStatus.GROUP);
    template.setAssociatedGroup("group");

    mockSecurity(USER);
    when(userGroupRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(TemplateException.class, () -> templateAccessHelper.templateCanBeAccessed(template));
  }

  @Test
  void templateCanBeAccessed_PublicTemplate_AlwaysTrue() {
    CompleteTemplate template = new CompleteTemplate();
    template.setAccessStatus(AccessStatus.PUBLIC);

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeAccessed(template);

    assertTrue(result);
  }

  @Test
  void templateCanBeBranched_NotLoggedInAndNotAllPermission_ReturnFalse() {
    CompleteTemplate template = new CompleteTemplate();
    template.setBranchPermission(BranchPermission.NONE);

    mockSecurity(null);

    var result = templateAccessHelper.templateCanBeBranched(template, null);

    assertFalse(result);
  }

  @Test
  void templateCanBeBranched_UserIsCreator_ReturnTrue() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(USER);
    template.setBranchPermission(BranchPermission.NONE);

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeBranched(template, null);

    assertTrue(result);
  }

  @Test
  void templateCanBeBranched_NoneCanBranch_ReturnFalse() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(SECOND_USER);
    template.setBranchPermission(BranchPermission.NONE);

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeBranched(template, null);

    assertFalse(result);
  }

  @Test
  void templateCanBeBranched_RequestOnlyUserIsSharedWith_ReturnTrue() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(SECOND_USER);
    template.setBranchPermission(BranchPermission.REQUEST_ONLY);
    template.setSharedWith(Set.of(USER));

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeBranched(template, null);

    assertTrue(result);
  }

  @Test
  void templateCanBeBranched_FriendsOnlyUserIsFriend_ReturnTrue() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(SECOND_USER);
    template.setBranchPermission(BranchPermission.FRIENDS_ONLY);

    TemplateUser user = new TemplateUser();
    user.setFriends(List.of(USER));

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeBranched(template, user);

    assertTrue(result);
  }

  @Test
  void templateCanBeBranched_GroupOnlyUserInGroup_ReturnTrue() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(SECOND_USER);
    template.setBranchPermission(BranchPermission.GROUP_ONLY);
    template.setAssociatedGroup("group");

    UserGroup userGroup = new UserGroup();
    userGroup.setMembers(Set.of(USER));

    mockSecurity(USER);
    when(userGroupRepository.findById(anyString())).thenReturn(Optional.of(userGroup));

    var result = templateAccessHelper.templateCanBeBranched(template, null);

    assertTrue(result);
  }

  @Test
  void templateCanBeBranched_AllCanBranchLoggedInUser_ReturnTrue() {
    CompleteTemplate template = new CompleteTemplate();
    template.setCreatorName(SECOND_USER);
    template.setBranchPermission(BranchPermission.ALL);

    mockSecurity(USER);

    var result = templateAccessHelper.templateCanBeBranched(template, null);

    assertTrue(result);
  }

  private void mockSecurity(String username) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }

}