package com.test.template.service;

import com.test.template.exceptions.TemplateException;
import com.test.template.models.categorization.Category;
import com.test.template.repository.CategoryRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static com.test.utils.ObjectsUtil.CATEGORY_NAME;
import static com.test.utils.ObjectsUtil.createCategory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategorizationServiceTest {

  private static final String NEW_CATEGORY_NAME = "New";
  private static final String[] TAGS = {"1", "2"};

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategorizationService categorizationService;

  @Test
  void getCategories_NoErrors_Success() {
    List<Category> categories = Collections.singletonList(createCategory());

    doReturn(categories).when(categoryRepository).findAll();

    var result = categorizationService.getCategories();

    assertEquals(categories, result);
  }

  @Test
  void getCategoryById_ValidId_Success() {
    Category category = createCategory();

    doReturn(Optional.of(category)).when(categoryRepository).findById(anyString());

    var result = categorizationService.getCategoryById("id");

    assertEquals(category, result);
  }

  @Test
  void getCategoryById_InvalidId_Exception() {
    doReturn(Optional.empty()).when(categoryRepository).findById(anyString());

    assertThrows(TemplateException.class, () -> categorizationService.getCategoryById("id"));
  }

  @Test
  void createCategory_NewCategory_Success() {
    String expectedMessage = String.format(" Adding tags: %s for category %s", String.join(",", TAGS), NEW_CATEGORY_NAME);

    doReturn(Optional.empty()).when(categoryRepository).findByCategoryName(NEW_CATEGORY_NAME);

    categorizationService.createCategory(NEW_CATEGORY_NAME, TAGS);

    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void createCategory_ExistingCategory_Success() {
    Category category = createCategory();
    category.setCategoryName(NEW_CATEGORY_NAME);
    category.setChildTags(new HashSet<>());

    String expectedMessage = String.format("Adding tags: %s for category %s", String.join(",", TAGS), NEW_CATEGORY_NAME);

    doReturn(Optional.of(category)).when(categoryRepository).findByCategoryName(NEW_CATEGORY_NAME);

    categorizationService.createCategory(NEW_CATEGORY_NAME, TAGS);

    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void deleteCategory_ExistingCategory_Success() {
    categorizationService.deleteCategory(CATEGORY_NAME);

    verify(categoryRepository).deleteCategoryByCategoryName(CATEGORY_NAME);
  }
}