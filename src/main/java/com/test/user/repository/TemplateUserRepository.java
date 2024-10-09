package com.test.user.repository;

import com.test.user.models.TemplateUser;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateUserRepository extends MongoRepository<TemplateUser, String> {

    Optional<TemplateUser> getTemplateUserByUsername(String username);

    @Query("{username: ?0}")
    @Update("{$push: {friends: ?1}}")
    void pushUserToFriendsList(String username, String friend);

    @Query("{username: ?0}")
    @Update("{$push: {groups: ?1}}")
    void addGroupToUser(String username, String groupId);

    @Query("{username: ?0}")
    @Update("{$pull: {groups: ?1}}")
    void pullGroupFromUser(String username, String groupId);

}
