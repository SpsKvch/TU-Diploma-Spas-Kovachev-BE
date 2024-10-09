package com.test.user.repository;

import com.test.security.jwt.JwtUtil;
import com.test.user.models.TemplateUser;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


import static com.test.utils.Constants.SPECIAL_CHARACTERS;

@Repository
@RequiredArgsConstructor
public class TemplateUserCustomRepository {

  private static final String USERNAME_KEY = "username";

  private final MongoTemplate mongoTemplate;

  public List<TemplateUser> searchUsers(String search) {
    String trimmedSearch = StringUtils.trim(search).toLowerCase().replaceAll(SPECIAL_CHARACTERS, StringUtils.EMPTY);
    String regex = "^.*" + trimmedSearch + ".*$";
    Query query = new Query();
    query.addCriteria(Criteria.where(USERNAME_KEY).regex(regex));
    return mongoTemplate.find(query, TemplateUser.class);
  }

  public List<TemplateUser> findRecentUsers(Integer count) {
    Query query = new Query();
    query.with(Sort.by(Sort.Direction.DESC, "joinDate"));
    query.limit(count == null ? 0 : count);
    addUserCriteria(query);
    return mongoTemplate.find(query, TemplateUser.class);
  }

  public List<TemplateUser> findAllUsersInList(Collection<String> users) {
    Query query = new Query();
    query.addCriteria(Criteria.where(USERNAME_KEY).in(users));
    return mongoTemplate.find(query, TemplateUser.class);
  }

  private void addUserCriteria(Query query) {
    String user = JwtUtil.getLoggedInUser();
    if (user != null) {
      query.addCriteria(Criteria.where(USERNAME_KEY).ne(user));
    }
  }

}
