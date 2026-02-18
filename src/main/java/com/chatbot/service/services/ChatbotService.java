package com.chatbot.service.services;

import com.chatbot.service.dto.ChatRequest;
import com.chatbot.service.dto.ChatResponse;
import com.chatbot.service.model.ChatMessage;
import com.chatbot.service.model.ChatSession;
import com.chatbot.service.repository.ChatSessionRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatbotService {

    private final ChatSessionRepository chatSessionRepository;
    private final OpenAiService openAiService;
    
    @Value("${openai.model}")
    private String model;
    
    @Value("${openai.initial-prompt}")
    private String initialPrompt;

    public ChatbotService(ChatSessionRepository chatSessionRepository,
                          @Value("${openai.api-key}") String apiKey) {
        this.chatSessionRepository = chatSessionRepository;
        this.openAiService = new OpenAiService(apiKey);
    }

    public ChatResponse sendMessage(ChatRequest request) {
        log.info("Processing chat request for userId: {}", request.getUserId());
        
        // Get or create session
        ChatSession session = getOrCreateSession(request.getUserId(), request.getSessionId());
        
        // Add user message to session
        ChatMessage userMessage = new ChatMessage(
            ChatMessageRole.USER.value(),
            request.getMessage(),
            LocalDateTime.now()
        );
        session.getMessages().add(userMessage);
        
        // Prepare messages for OpenAI
        List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = prepareOpenAiMessages(session, request.getContextData());
        
        // Call OpenAI API
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
            .model(model)
            .messages(openAiMessages)
            .build();
        
        ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
        String assistantResponse = result.getChoices().get(0).getMessage().getContent();
        
        // Add assistant response to session
        ChatMessage assistantMessage = new ChatMessage(
            ChatMessageRole.ASSISTANT.value(),
            assistantResponse,
            LocalDateTime.now()
        );
        session.getMessages().add(assistantMessage);
        
        // Update session
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        
        log.info("Successfully processed chat request for session: {}", session.getSessionId());
        
        return new ChatResponse(
            session.getSessionId(),
            assistantResponse,
            request.getUserId(),
            System.currentTimeMillis()
        );
    }

    private ChatSession getOrCreateSession(String userId, String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            return chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseGet(() -> createNewSession(userId));
        }
        return createNewSession(userId);
    }

    private ChatSession createNewSession(String userId) {
        ChatSession session = new ChatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setMessages(new ArrayList<>());
        session.setInitialPrompt(initialPrompt);
        
        log.info("Created new session: {} for userId: {}", session.getSessionId(), userId);
        return session;
    }

    private List<com.theokanning.openai.completion.chat.ChatMessage> prepareOpenAiMessages(
            ChatSession session, String contextData) {
        
        List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
        
        // Add system message with initial prompt and context
        String systemContent = session.getInitialPrompt();
        if (contextData != null && !contextData.isEmpty()) {
            systemContent += "\n\nContext: " + contextData;
        }
        messages.add(new com.theokanning.openai.completion.chat.ChatMessage(
            ChatMessageRole.SYSTEM.value(),
            systemContent
        ));
        
        // Add conversation history
        messages.addAll(session.getMessages().stream()
            .map(msg -> new com.theokanning.openai.completion.chat.ChatMessage(
                msg.getRole(),
                msg.getContent()
            ))
            .collect(Collectors.toList()));
        
        return messages;
    }

    public List<ChatSession> getUserSessions(String userId) {
        return chatSessionRepository.findByUserId(userId);
    }

    public ChatSession getSession(String sessionId) {
        return chatSessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }
}
