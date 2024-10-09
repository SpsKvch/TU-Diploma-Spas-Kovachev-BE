package com.test.template.repository;

import com.test.template.models.tracked.TrackedTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedTemplateRepository extends MongoRepository<TrackedTemplate, String> {

    List<TrackedTemplate> getTrackedTemplatesByOwnerName(String ownerName);

    List<TrackedTemplate> getTrackedTemplateByOwnerName(String ownerName, Pageable pageable);

    Optional<TrackedTemplate> findByOwnerNameAndOriginalTemplateId(String ownerName, String templateId);

    boolean existsByOwnerNameAndOriginalTemplateId(String ownerName, String templateId);

    long deleteDistinctByIdAndOwnerName(String id, String ownerName);

}
