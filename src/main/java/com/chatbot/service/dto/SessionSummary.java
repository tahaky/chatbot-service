package com.chatbot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummary {
    
    private String sessionId;
    
    private String userId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private int messageCount;
    
    private String lastMessage;
    
    private String lastMessageRole;
}
