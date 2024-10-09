package com.test.user.mapper;

import com.test.template.models.complete.CompleteTemplate;
import com.test.user.models.SimpleUserResponse;
import com.test.user.models.TemplateUser;
import com.test.user.models.TemplateUserResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import static org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface TemplateUserMapper {

  @Mapping(target = "email", source = "currentEmail")
  SimpleUserResponse toLoginResponse(TemplateUser user);

  List<TemplateUserResponse> toPublicResponseList(List<TemplateUser> user);

  TemplateUserResponse toPublicResponse(TemplateUser user);

}
