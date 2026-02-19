package com.chatbot.service.dto.forum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumSubthreadResponse {
    private String id;
    private String userId;
    private String title;
    private String createdAt;
    @JsonProperty("initalMessage")
    private String initialMessage;
    private String threadId;
    private List<ForumMessageResponse> messages;
}
