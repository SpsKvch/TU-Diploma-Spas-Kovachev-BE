package com.test.template.repository;

import com.test.template.models.categorization.Category;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

  Optional<Category> findByCategoryName(String categoryName);

  void deleteCategoryByCategoryName(String categoryName);

}
