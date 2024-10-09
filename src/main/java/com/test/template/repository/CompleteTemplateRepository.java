package com.test.template.repository;

import com.test.template.models.complete.CompleteTemplate;
import com.test.template.models.enums.AccessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;

public interface CompleteTemplateRepository extends MongoRepository<CompleteTemplate, String> {

    @Query("{_id: ObjectId(?0)}")
    @Update("{$inc: {views: 1}}")
    void incrementTemplateViews(String templateId);

    @Query("{_id: ObjectId(?0)}")
    @Update("{$inc: {approvals: ?1, totalEngagements:  ?2}}")
    void incrementApprovalsAndEngagements(String templateId, int approvalInc, int engagementInc);

    @Query("{_id: ObjectId(?0)}")
    @Update("{$inc: {branches:  1}}")
    void incrementBranches(String templateId);

    @Query("{_id: ObjectId(?0)}")
    @Update("{$set: {sharedWith:  ?1}}")
    void updateSharedWith(String templateId, List<String> sharedWith);

    List<CompleteTemplate> getCompleteTemplatesByCreatorName(String creatorName);
    List<CompleteTemplate> getCompleteTemplatesByCreatorNameAndAccessStatus(String creatorName, AccessStatus accessStatus, Pageable pageable);

    @Query("{accessStatus: ?0}")
    Page<CompleteTemplate> getAllByAccessStatus(AccessStatus status, Pageable pageable);

}
