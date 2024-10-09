package com.test.security.users.repository;

import com.test.security.users.model.UserDetailsImpl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends MongoRepository<UserDetailsImpl, String> {

    Optional<UserDetails> getUserDetailsImplByUsername(String username);

    boolean existsByUsername(String username);

}
