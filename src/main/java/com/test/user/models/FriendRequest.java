package com.test.user.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(unique = true, def = "sender: 1, recipient: 1", name = "friend_request_index")
public class FriendRequest {

    @Id
    private String id;
    private String sender;
    private String senderImageUrl;
    private String recipient;
    private String recipientImageUrl;
    private Date sendDate;

}
