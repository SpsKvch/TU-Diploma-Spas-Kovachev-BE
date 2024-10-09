package com.test.user.models.groups;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {

    @Id
    private String id;
    @Indexed(unique = true)
    private String groupNameRaw;
    private String groupNameActual;
    private String description;
    private String creator;
    private Set<String> members;
    private Date createdDate;

}
