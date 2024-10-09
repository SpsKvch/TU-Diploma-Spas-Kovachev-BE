package com.test.template.mappers;

import com.test.template.models.steps.TemplateStep;
import com.test.template.models.steps.TemplateStepRequest;
import com.test.template.models.steps.TrackedStep;
import com.test.template.models.steps.TrackedStepRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "SPRING")
public interface TemplateStepMapper {

  TemplateStep toStep(TemplateStepRequest request);

  @Mapping(target = "markedUpTitle", source = "title")
  TrackedStep toTrackedStep(TrackedStepRequest request);

  @Mapping(target = "progressionStatus", expression = "java(com.test.template.models.enums.ProgressionStatus.NOT_STARTED)")
  TrackedStep toEmptyTrackedStep(TemplateStep step);

}
