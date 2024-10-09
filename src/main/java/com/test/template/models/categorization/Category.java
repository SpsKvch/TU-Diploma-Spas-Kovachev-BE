package com.test.template.models.categorization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
//@CompoundIndex(name = "category_index", unique = true, def = "categoryName: 1")
public class Category {

    @Id
    private String id;
    @Indexed(name = "category_index", unique = true)
    private String categoryName;
    private Set<String> childTags;
    private LocalDateTime creationTime;
    private LocalDateTime updateTime;

}
