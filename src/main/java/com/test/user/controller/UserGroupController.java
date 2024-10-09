package com.test.user.controller;

import com.test.user.models.groups.UserGroup;
import com.test.user.models.groups.UserGroupRequest;
import com.test.user.service.IUserGroupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/v1")
public class UserGroupController {

    private final IUserGroupService userGroupService;

    @PostMapping("/users/groups")
    public UserGroup createUserGroup(@Valid @RequestBody UserGroupRequest request) {
        return userGroupService.createUserGroup(request);
    }

    @PatchMapping("/users/groups/{groupId}")
    public void updateGroupContents(@NotBlank @PathVariable String groupId,
                                    @Valid @RequestBody UserGroupRequest request) {
        userGroupService.updateUserGroup(groupId, request);
    }

    @PatchMapping("/users/groups/{groupId}/join")
    public void joinGroup(@NotBlank @PathVariable String groupId) {
        userGroupService.joinGroup(groupId);
    }

    @GetMapping("/users/groups/{groupId}")
    public UserGroup getUserGroup(@NotBlank @PathVariable String groupId) {
        return userGroupService.getUserGroupById(groupId);
    }

    @DeleteMapping("/users/groups/{groupId}")
    public void leaveGroup(@NotBlank @PathVariable String groupId) {
        userGroupService.leaveGroup(groupId);
    }

}
