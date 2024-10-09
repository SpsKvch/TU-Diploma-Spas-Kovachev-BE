package com.test.template.service;

import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.complete.CompleteTemplateFilters;
import com.test.template.models.enums.AccessStatus;
import com.test.template.service.helpers.TemplateFilterPredicateAggregator;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TemplateQueryService {

  private final MongoTemplate mongoTemplate;

  public Page<CompleteTemplate> findCompleteTemplatesFiltered(CompleteTemplateFilters filters) {

    List<CompleteTemplate> fetchedTemplates = fetchFilteredTemplates(filters);
    TemplateFilterPredicateAggregator aggregator = new TemplateFilterPredicateAggregator(filters);

    if (aggregator.isEmpty()) {
      return new PageImpl<>(fetchedTemplates);
    }

    List<CompleteTemplate> filteredTemplates = new ArrayList<>(fetchedTemplates.size());

    fetchedTemplates.forEach(template -> {
      if (aggregator.execute(template)) {
        filteredTemplates.add(template);
      }
    });

    return new PageImpl<>(filteredTemplates);
  }

  private List<CompleteTemplate> fetchFilteredTemplates(CompleteTemplateFilters filters) {
    Query filteredQuery = new Query();
    filteredQuery.with(Sort.by(Sort.Direction.DESC, "createTime"));
    filteredQuery.addCriteria(Criteria.where("accessStatus").is(AccessStatus.PUBLIC));

    String title = StringUtils.strip(filters.getTitle());
    if (!StringUtils.isBlank(title)) {
      filteredQuery.addCriteria(Criteria.where("title").regex(".* " + title + " .*"));
    }

    if (!StringUtils.isBlank(filters.getCategoryName())) {
      filteredQuery.addCriteria(Criteria.where("category").is(filters.getCategoryName()));
      if (filters.getTags() != null && filters.getTags().size() == 1) {
        filteredQuery.addCriteria(buildTagsCriteria(filters.getTags()));
      }
    }

    if (filters.getMinDate() != null) {
      LocalDate maxDate;

      if (filters.getMaxDate() != null) {
        maxDate = filters.getMaxDate();
      } else {
        maxDate = LocalDate.now().plusDays(1);
      }

      filteredQuery.addCriteria(Criteria.where("createTime").gte(Date.valueOf(filters.getMinDate()))
              .andOperator(Criteria.where("createTime").lte(Date.valueOf(maxDate))));
    }

    if (Boolean.TRUE.equals(filters.getIsOriginal())) {
      filteredQuery.addCriteria(Criteria.where("parentDetails").exists(false));
    }

    return mongoTemplate.find(filteredQuery, CompleteTemplate.class);
  }

  private Criteria buildTagsCriteria(Set<String> tags) {
    Criteria tagCriteria = new Criteria();
    for (String tag : tags) {
      tagCriteria = tagCriteria.orOperator(Criteria.where("tags").is(tag));
    }
    return tagCriteria;
  }

}
