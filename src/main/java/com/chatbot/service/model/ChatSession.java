package com.chatbot.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_sessions")
public class ChatSession {
    
    @Id
    private String id;
    
    private String sessionId;
    
    private String userId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<ChatMessage> messages = new ArrayList<>();
    
    private String initialPrompt;
}
