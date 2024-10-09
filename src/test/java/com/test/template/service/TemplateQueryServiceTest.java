package com.test.template.service;

import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CompleteTemplateFilters;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;


import static com.test.utils.ObjectsUtil.createCompleteTemplate;
import static com.test.utils.ObjectsUtil.createTemplateFilters;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateQueryServiceTest {

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private TemplateQueryService templateQueryService;

  @Test
  void findCompleteTemplatesFiltered_AllFilters_Success() {
    CompleteTemplateFilters templateFilters = createTemplateFilters();
    CompleteTemplate template = createCompleteTemplate();
    setAdditionalTemplateProperties(template);

    when(mongoTemplate.find(any(), any())).thenReturn(Collections.singletonList(template));

    var result = templateQueryService.findCompleteTemplatesFiltered(templateFilters);

    assertEquals(1, result.getContent().size());
    assertEquals(template, result.getContent().get(0));
  }

  @Test
  void findCompleteTemplatesFiltered_NoFilters_QueryResultReturned() {
    CompleteTemplateFilters templateFilters = createTemplateFilters();
    templateFilters.setMaxCompletionTime(null);
    templateFilters.setTags(null);
    templateFilters.setMinApprovalPercent(0);
    templateFilters.setMinCompletionRate(0);
    templateFilters.setMaxDate(null);

    CompleteTemplate template = createCompleteTemplate();
    setAdditionalTemplateProperties(template);

    when(mongoTemplate.find(any(), any())).thenReturn(Collections.singletonList(template));

    var result = templateQueryService.findCompleteTemplatesFiltered(templateFilters);

    assertEquals(template, result.getContent().get(0));
  }

  private void setAdditionalTemplateProperties(CompleteTemplate template) {
    template.setCreateTime(LocalDateTime.now());
    template.setApprovals(10);
    template.setTotalEngagements(15);
    template.setCompletedJournals(5);
    template.setTotalJournals(6);
  }

}