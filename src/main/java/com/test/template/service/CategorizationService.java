package com.test.template.service;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.categorization.Category;
import com.test.template.repository.CategoryRepository;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CategorizationService {

  private final static String NO_CATEGORY_FOUND_ERROR_MESSAGE = "No category: %s exists";

  private final CategoryRepository categoryRepository;

  public List<Category> getCategories() {
    log.info("Fetching all categories");
    return categoryRepository.findAll();
  }

  public Category getCategoryById(String id) {
    log.info("Fetching category with id: {}", id);
    return categoryRepository.findById(id).orElseThrow(
            () -> new TemplateException(String.format(NO_CATEGORY_FOUND_ERROR_MESSAGE, id), HttpStatus.NOT_FOUND));
  }

  public void createCategory(String categoryName, String[] tags) {
    Category category = categoryRepository.findByCategoryName(categoryName).orElse(null);
    if (category == null) {
      category = new Category();
      category.setCategoryName(categoryName);
      category.setChildTags(new HashSet<>());
      category.setCreationTime(LocalDateTime.now());
    }

    Set<String> lowercaseTags = new HashSet<>(Arrays.asList(tags)).stream()
        .map(String::toLowerCase).map(StringUtils::trim)
        .collect(Collectors.toSet());

    category.getChildTags().addAll(lowercaseTags);
    category.setUpdateTime(LocalDateTime.now());

    String tagsList = String.join(",", lowercaseTags);
    log.info("Adding tags: {} for category {}", tagsList, category.getCategoryName());

    categoryRepository.save(category);
  }

  public void deleteCategory(String categoryName) {
    log.info("Deleting category: {}", categoryName);
    categoryRepository.deleteCategoryByCategoryName(categoryName);
  }

}
