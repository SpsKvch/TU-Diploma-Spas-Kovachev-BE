package com.test.template.repository;

import com.test.template.models.draft.TemplateDraft;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateDraftRepository extends MongoRepository<TemplateDraft, String> {

    List<TemplateDraft> getTemplateDraftsByCreatorName(String creatorName);
    List<TemplateDraft> getTemplateDraftsByCreatorName(String creatorName, Pageable pageable);

}
