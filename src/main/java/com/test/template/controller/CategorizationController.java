package com.test.template.controller;

import com.test.template.models.categorization.Category;
import com.test.template.service.CategorizationService;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/v1/categories")
public class CategorizationController {

  private final CategorizationService categorizationService;

  @PutMapping("/{categoryName}")
  public void createCategory(@PathVariable @NotBlank String categoryName, @RequestBody String[] tags) {
    categorizationService.createCategory(categoryName, tags);
  }

  @GetMapping
  public List<Category> getAllCategories() {
    return categorizationService.getCategories();
  }

  @GetMapping("/{categoryId}")
  public Category getCategoryById(@PathVariable String categoryId) {
    return categorizationService.getCategoryById(categoryId);
  }

  @DeleteMapping("/{categoryName}")
  public void deleteCategory(@PathVariable String categoryName) {
    categorizationService.deleteCategory(categoryName);
  }

}
