package com.test.user.repository;

import com.test.user.models.FriendRequest;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    List<FriendRequest> findAllBySender(String sender);

    List<FriendRequest> findAllByRecipient(String recipient);
    Optional<FriendRequest> findBySenderAndRecipient(String sender, String recipient);

    boolean existsBySenderAndRecipient(String sender, String recipient);

    void deleteBySenderAndRecipient(String sender, String recipient);

}
