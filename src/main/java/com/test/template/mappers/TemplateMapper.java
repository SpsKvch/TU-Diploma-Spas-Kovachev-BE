package com.test.template.mappers;

import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.SimpleCompleteTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "SPRING")
public interface TemplateMapper {

    @Mapping(target = "parentId", source = "completeTemplate.parentDetails.parentId")
    SimpleCompleteTemplate toSimpleTemplate(CompleteTemplate completeTemplate);

}
