package com.chatbot.service.controller;

import com.chatbot.service.dto.ChatRequest;
import com.chatbot.service.dto.ChatResponse;
import com.chatbot.service.dto.ContinueChatRequest;
import com.chatbot.service.dto.SessionSummary;
import com.chatbot.service.model.ChatMessage;
import com.chatbot.service.model.ChatSession;
import com.chatbot.service.services.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "OpenAI Chat API endpoints")
public class ChatController {

    private final ChatbotService chatbotService;

    @PostMapping("/send")
    @Operation(summary = "Send a message to the chatbot", 
               description = "Send a message and receive an AI-generated response. Optionally provide session ID to continue an existing conversation.")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{userId}")
    @Operation(summary = "Get user sessions", 
               description = "Retrieve all chat sessions for a specific user")
    public ResponseEntity<List<ChatSession>> getUserSessions(@PathVariable String userId) {
        List<ChatSession> sessions = chatbotService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get session details", 
               description = "Retrieve details of a specific chat session including full message history")
    public ResponseEntity<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = chatbotService.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/continue")
    @Operation(summary = "Continue an existing conversation", 
               description = "Continue a chat session by providing sessionId and a new message. This endpoint explicitly validates the session exists.")
    public ResponseEntity<ChatResponse> continueConversation(@Valid @RequestBody ContinueChatRequest request) {
        ChatResponse response = chatbotService.continueConversation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{userId}/summaries")
    @Operation(summary = "Get user session summaries", 
               description = "Retrieve session summaries for a user (without full message history). Useful for listing conversations.")
    public ResponseEntity<List<SessionSummary>> getUserSessionSummaries(@PathVariable String userId) {
        List<SessionSummary> summaries = chatbotService.getUserSessionSummaries(userId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/session/{sessionId}/history")
    @Operation(summary = "Get session message history", 
               description = "Retrieve message history for a session. Use limit parameter to get recent messages only.")
    public ResponseEntity<List<ChatMessage>> getSessionHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int limit) {
        List<ChatMessage> history = chatbotService.getSessionHistory(sessionId, limit);
        return ResponseEntity.ok(history);
    }
}
