package com.chatbot.service.dto.forum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumMessageResponse {
    private String id;
    private String userId;
    private String body;
    private String createdAt;
    private int upvoteCount;
    private boolean deleted;
    private String updatedAt;
    private String subthreadId;
}
