package com.chatbot.service.services;

import com.chatbot.service.dto.ChatRequest;
import com.chatbot.service.dto.ChatResponse;
import com.chatbot.service.dto.ContinueChatRequest;
import com.chatbot.service.dto.SessionSummary;
import com.chatbot.service.exception.OpenAiException;
import com.chatbot.service.exception.SessionNotFoundException;
import com.chatbot.service.model.ChatMessage;
import com.chatbot.service.model.ChatSession;
import com.chatbot.service.repository.ChatSessionRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatSessionRepository chatSessionRepository;
    private final OpenAiService openAiService;
    
    @Value("${openai.model}")
    private String model;
    
    @Value("${openai.initial-prompt}")
    private String initialPrompt;

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
        
        // Validate response
        if (result.getChoices() == null || result.getChoices().isEmpty()) {
            throw new OpenAiException("No response received from OpenAI API");
        }
        
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
                .orElseThrow(() -> new SessionNotFoundException(
                    "Session not found or does not belong to user: " + sessionId));
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
            .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
    }

    /**
     * Continue an existing chat session with a new message.
     * If the session doesn't exist and userId is provided, a new session will be created.
     * 
     * @param request The continue chat request containing sessionId and message
     * @return ChatResponse with the AI's response
     */
    public ChatResponse continueConversation(ContinueChatRequest request) {
        log.info("Continuing conversation for session: {}", request.getSessionId());
        
        // Try to get existing session, or create new one if userId is provided
        ChatSession session = chatSessionRepository.findBySessionId(request.getSessionId())
            .orElseGet(() -> {
                if (request.getUserId() != null && !request.getUserId().isEmpty()) {
                    log.info("Session not found, creating new session for userId: {}", request.getUserId());
                    return createNewSession(request.getUserId());
                } else {
                    throw new SessionNotFoundException("Session not found: " + request.getSessionId());
                }
            });
        
        // Add user message to session
        ChatMessage userMessage = new ChatMessage(
            ChatMessageRole.USER.value(),
            request.getMessage(),
            LocalDateTime.now()
        );
        session.getMessages().add(userMessage);
        
        // Prepare messages for OpenAI
        List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = 
            prepareOpenAiMessages(session, request.getContextData());
        
        // Call OpenAI API
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
            .model(model)
            .messages(openAiMessages)
            .build();
        
        ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
        
        // Validate response
        if (result.getChoices() == null || result.getChoices().isEmpty()) {
            throw new OpenAiException("No response received from OpenAI API");
        }
        
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
        
        log.info("Successfully continued conversation for session: {}", session.getSessionId());
        
        return new ChatResponse(
            session.getSessionId(),
            assistantResponse,
            session.getUserId(),
            System.currentTimeMillis()
        );
    }

    /**
     * Get session summaries for a user (without full message history).
     * Useful for displaying a list of conversations.
     * 
     * @param userId The user ID
     * @return List of session summaries
     */
    public List<SessionSummary> getUserSessionSummaries(String userId) {
        log.info("Getting session summaries for userId: {}", userId);
        
        List<ChatSession> sessions = chatSessionRepository.findByUserId(userId);
        
        return sessions.stream()
            .map(this::convertToSummary)
            .collect(Collectors.toList());
    }

    /**
     * Get message history for a session with optional pagination.
     * 
     * @param sessionId The session ID
     * @param limit Maximum number of recent messages to return (0 for all)
     * @return List of chat messages
     */
    public List<ChatMessage> getSessionHistory(String sessionId, int limit) {
        log.info("Getting history for session: {} with limit: {}", sessionId, limit);
        
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
        
        List<ChatMessage> messages = session.getMessages();
        
        if (limit > 0 && messages.size() > limit) {
            // Return the most recent 'limit' messages
            return messages.subList(Math.max(0, messages.size() - limit), messages.size());
        }
        
        return messages;
    }

    private SessionSummary convertToSummary(ChatSession session) {
        SessionSummary summary = new SessionSummary();
        summary.setSessionId(session.getSessionId());
        summary.setUserId(session.getUserId());
        summary.setCreatedAt(session.getCreatedAt());
        summary.setUpdatedAt(session.getUpdatedAt());
        summary.setMessageCount(session.getMessages().size());
        
        // Get last message info
        if (!session.getMessages().isEmpty()) {
            ChatMessage lastMessage = session.getMessages().get(session.getMessages().size() - 1);
            summary.setLastMessage(
                lastMessage.getContent().length() > 100 
                    ? lastMessage.getContent().substring(0, 100) + "..." 
                    : lastMessage.getContent()
            );
            summary.setLastMessageRole(lastMessage.getRole());
        }
        
        return summary;
    }
}
