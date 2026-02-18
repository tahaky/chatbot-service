package com.chatbot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContinueChatRequest {
    
    @NotBlank(message = "Session ID is required to continue conversation")
    private String sessionId;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String contextData;
}
