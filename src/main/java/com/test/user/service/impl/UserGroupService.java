package com.test.user.service.impl;

import com.test.security.jwt.JwtUtil;
import com.test.user.exception.TemplateUserException;
import com.test.user.models.groups.UserGroup;
import com.test.user.models.groups.UserGroupRequest;
import com.test.user.repository.TemplateUserRepository;
import com.test.user.repository.UserGroupRepository;
import com.test.user.service.IUserGroupService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserGroupService implements IUserGroupService {

  private static final String NO_GROUP_FOUND = "Unable to find group with id: %s";

  private final TemplateUserRepository templateUserRepository;
  private final UserGroupRepository groupRepository;

  @Override
  public UserGroup createUserGroup(final UserGroupRequest request) {
    String user = JwtUtil.getLoggedInUser();
    UserGroup createdGroup = UserGroup.builder()
        .groupNameRaw(StringUtils.strip(request.getName().toLowerCase()))
        .groupNameActual(request.getName())
        .description(request.getDescription())
        .creator(user)
        .members(Collections.singleton(user))
        .createdDate(Date.valueOf(LocalDate.now()))
        .build();
    UserGroup savedGroup = groupRepository.save(createdGroup);
    templateUserRepository.addGroupToUser(user, savedGroup.getId());
    return savedGroup;
  }

  @Override
  public void updateUserGroup(final String groupId, final UserGroupRequest request) {
    groupRepository.updateUserGroup(groupId, request.getName(), request.getDescription());
  }

  @Override
  public void joinGroup(final String groupId) {
    UserGroup groupToJoin = fetchUserGroup(groupId);
    String user = JwtUtil.getLoggedInUser();
    if (groupToJoin.getCreator().equals(user) || groupToJoin.getMembers().contains(user)) {
      throw new TemplateUserException("User is already part of the group", HttpStatus.BAD_REQUEST);
    }
    groupToJoin.getMembers().add(user);
    groupRepository.save(groupToJoin);
  }

  @Override
  public UserGroup getUserGroupById(final String groupId) {
    return groupRepository.findById(groupId)
        .orElseThrow(() -> new TemplateUserException(String.format(NO_GROUP_FOUND, groupId), HttpStatus.NOT_FOUND));
  }

  @Override
  public void leaveGroup(final String groupId) {
    UserGroup groupToLeave = fetchUserGroup(groupId);
    String user = JwtUtil.getLoggedInUser();
    if (groupToLeave.getCreator().equals(user)) {
      throw new TemplateUserException("Group creator cannot leave the group", HttpStatus.BAD_REQUEST);
    }
    groupToLeave.getMembers().remove(user);
    templateUserRepository.pullGroupFromUser(user, groupId);
    groupRepository.save(groupToLeave);
  }

  private UserGroup fetchUserGroup(final String groupId) {
    return groupRepository.findById(groupId)
        .orElseThrow(() -> new TemplateUserException(String.format(NO_GROUP_FOUND, groupId), HttpStatus.BAD_REQUEST));
  }

}
