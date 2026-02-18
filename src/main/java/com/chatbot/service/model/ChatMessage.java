package com.chatbot.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private String role; // "system", "user", "assistant"
    
    private String content;
    
    private LocalDateTime timestamp;
}
