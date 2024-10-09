package com.test.user.service;

import com.test.user.models.groups.UserGroup;
import com.test.user.models.groups.UserGroupRequest;

public interface IUserGroupService {
    UserGroup createUserGroup(UserGroupRequest request);

    void updateUserGroup(String groupId, UserGroupRequest request);

    void joinGroup(String groupId);

    UserGroup getUserGroupById(String groupId);

    void leaveGroup(String groupId);
}
