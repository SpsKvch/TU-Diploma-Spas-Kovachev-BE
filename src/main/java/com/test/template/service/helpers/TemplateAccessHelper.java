package com.test.template.service.helpers;

import com.test.security.jwt.JwtUtil;
import com.test.template.exceptions.TemplateException;
import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.enums.BranchPermission;
import com.test.user.models.TemplateUser;
import com.test.user.models.groups.UserGroup;
import com.test.user.repository.UserGroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class TemplateAccessHelper {

  private final UserGroupRepository groupRepository;

  public boolean templateCanBeAccessed(CompleteTemplate template) {
    String user = JwtUtil.getLoggedInUser();
    switch (template.getAccessStatus()) {
      case PERSONAL -> {
        return template.getCreatorName().equals(user);
      }
      case PRIVATE -> {
        return template.getSharedWith().contains(user) || template.getCreatorName().equals(user);
      }
      case GROUP -> {
        return userInTemplateGroup(template.getAssociatedGroup(), user);
      }
      case PUBLIC -> {
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  public boolean templateCanBeBranched(CompleteTemplate template, TemplateUser templateCreator) {
    String user = JwtUtil.getLoggedInUser();
    if (user == null) {
      return BranchPermission.ALL == template.getBranchPermission();
    }

    if (user.equals(template.getCreatorName())) {
      return true;
    }

    return switch (template.getBranchPermission()) {
      case NONE -> false;
      case REQUEST_ONLY -> template.getSharedWith().contains(user);
      case FRIENDS_ONLY -> Objects.requireNonNull(templateCreator).getFriends().contains(user);
      case GROUP_ONLY -> userInTemplateGroup(template.getAssociatedGroup(), user);
      case ALL -> true;
    };
  }

  private boolean userInTemplateGroup(final String groupId, final String user) {
    UserGroup associatedGroup = groupRepository.findById(groupId).orElseThrow(
        () -> new TemplateException("Cannot branch from group as template is not associated with any", HttpStatus.INTERNAL_SERVER_ERROR));
    return associatedGroup.getMembers().contains(user);
  }

}
