package com.test.user.repository;

import com.test.user.models.groups.UserGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends MongoRepository<UserGroup, String> {

    @Query("{_id: ObjectId(?0)}")
    @Update("{$set: {groupName: ?1, description: ?2}}")
    void updateUserGroup(String groupId, String groupName, String description);

}
