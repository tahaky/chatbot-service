package com.chatbot.service;

import com.chatbot.service.dto.ContinueChatRequest;
import com.chatbot.service.dto.SessionSummary;
import com.chatbot.service.exception.SessionNotFoundException;
import com.chatbot.service.model.ChatMessage;
import com.chatbot.service.model.ChatSession;
import com.chatbot.service.repository.ChatSessionRepository;
import com.chatbot.service.services.ChatbotService;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "openai.api-key=test-key",
    "spring.data.mongodb.uri=mongodb://localhost:27017/test"
})
class ChatHistoryFeatureTests {

    @Autowired
    private ChatbotService chatbotService;

    @MockBean
    private ChatSessionRepository chatSessionRepository;

    @MockBean
    private OpenAiService openAiService;

    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        // Create a test session with message history
        testSession = new ChatSession();
        testSession.setId("test-id");
        testSession.setSessionId("test-session-123");
        testSession.setUserId("user123");
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setUpdatedAt(LocalDateTime.now());
        testSession.setInitialPrompt("You are a helpful assistant.");
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello", LocalDateTime.now()));
        messages.add(new ChatMessage("assistant", "Hi there!", LocalDateTime.now()));
        testSession.setMessages(messages);
    }

    @Test
    void testGetUserSessionSummaries() {
        // Arrange
        List<ChatSession> sessions = Arrays.asList(testSession);
        when(chatSessionRepository.findByUserId("user123")).thenReturn(sessions);

        // Act
        List<SessionSummary> summaries = chatbotService.getUserSessionSummaries("user123");

        // Assert
        assertNotNull(summaries);
        assertEquals(1, summaries.size());
        SessionSummary summary = summaries.get(0);
        assertEquals("test-session-123", summary.getSessionId());
        assertEquals("user123", summary.getUserId());
        assertEquals(2, summary.getMessageCount());
        assertEquals("Hi there!", summary.getLastMessage());
        assertEquals("assistant", summary.getLastMessageRole());
        
        verify(chatSessionRepository).findByUserId("user123");
    }

    @Test
    void testGetSessionHistory() {
        // Arrange
        when(chatSessionRepository.findBySessionId("test-session-123"))
            .thenReturn(Optional.of(testSession));

        // Act
        List<ChatMessage> history = chatbotService.getSessionHistory("test-session-123", 0);

        // Assert
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("Hello", history.get(0).getContent());
        assertEquals("Hi there!", history.get(1).getContent());
        
        verify(chatSessionRepository).findBySessionId("test-session-123");
    }

    @Test
    void testGetSessionHistoryWithLimit() {
        // Arrange
        // Add more messages
        testSession.getMessages().add(new ChatMessage("user", "How are you?", LocalDateTime.now()));
        testSession.getMessages().add(new ChatMessage("assistant", "I'm good!", LocalDateTime.now()));
        
        when(chatSessionRepository.findBySessionId("test-session-123"))
            .thenReturn(Optional.of(testSession));

        // Act - get only last 2 messages
        List<ChatMessage> history = chatbotService.getSessionHistory("test-session-123", 2);

        // Assert
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("How are you?", history.get(0).getContent());
        assertEquals("I'm good!", history.get(1).getContent());
    }

    @Test
    void testGetSessionHistoryNotFound() {
        // Arrange
        when(chatSessionRepository.findBySessionId("invalid-session"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SessionNotFoundException.class, () -> {
            chatbotService.getSessionHistory("invalid-session", 0);
        });
    }

    @Test
    void testContinueConversationSuccess() {
        // Arrange
        when(chatSessionRepository.findBySessionId("test-session-123"))
            .thenReturn(Optional.of(testSession));
        
        // Mock OpenAI response
        ChatCompletionResult mockResult = mock(ChatCompletionResult.class);
        ChatCompletionChoice mockChoice = mock(ChatCompletionChoice.class);
        com.theokanning.openai.completion.chat.ChatMessage mockMessage = 
            new com.theokanning.openai.completion.chat.ChatMessage();
        mockMessage.setContent("Sure, I can help with that!");
        
        when(mockChoice.getMessage()).thenReturn(mockMessage);
        when(mockResult.getChoices()).thenReturn(Arrays.asList(mockChoice));
        when(openAiService.createChatCompletion(any())).thenReturn(mockResult);
        when(chatSessionRepository.save(any())).thenReturn(testSession);

        // Act
        ContinueChatRequest request = new ContinueChatRequest();
        request.setSessionId("test-session-123");
        request.setMessage("Can you help me?");
        
        var response = chatbotService.continueConversation(request);

        // Assert
        assertNotNull(response);
        assertEquals("test-session-123", response.getSessionId());
        assertEquals("Sure, I can help with that!", response.getMessage());
        assertEquals("user123", response.getUserId());
        
        verify(chatSessionRepository).findBySessionId("test-session-123");
        verify(chatSessionRepository).save(any(ChatSession.class));
    }

    @Test
    void testContinueConversationSessionNotFound() {
        // Arrange
        when(chatSessionRepository.findBySessionId("invalid-session"))
            .thenReturn(Optional.empty());

        // Act & Assert
        ContinueChatRequest request = new ContinueChatRequest();
        request.setSessionId("invalid-session");
        request.setMessage("Hello");
        // No userId provided, should throw exception
        
        assertThrows(SessionNotFoundException.class, () -> {
            chatbotService.continueConversation(request);
        });
    }

    @Test
    void testContinueConversationCreatesNewSessionWhenNotFoundWithUserId() {
        // Arrange
        when(chatSessionRepository.findBySessionId("non-existent-session"))
            .thenReturn(Optional.empty());
        
        ChatSession newSession = new ChatSession();
        newSession.setSessionId("new-session-123");
        newSession.setUserId("user456");
        newSession.setMessages(new ArrayList<>());
        newSession.setInitialPrompt("You are a helpful assistant.");
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setUpdatedAt(LocalDateTime.now());
        
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(newSession);
        
        // Mock OpenAI response
        ChatCompletionResult mockResult = mock(ChatCompletionResult.class);
        ChatCompletionChoice mockChoice = mock(ChatCompletionChoice.class);
        com.theokanning.openai.completion.chat.ChatMessage mockMessage = 
            new com.theokanning.openai.completion.chat.ChatMessage();
        mockMessage.setContent("Hello! How can I help you?");
        
        when(mockChoice.getMessage()).thenReturn(mockMessage);
        when(mockResult.getChoices()).thenReturn(Arrays.asList(mockChoice));
        when(openAiService.createChatCompletion(any())).thenReturn(mockResult);

        // Act
        ContinueChatRequest request = new ContinueChatRequest();
        request.setSessionId("non-existent-session");
        request.setMessage("Hello");
        request.setUserId("user456");
        
        var response = chatbotService.continueConversation(request);

        // Assert
        assertNotNull(response);
        assertEquals("Hello! How can I help you?", response.getMessage());
        assertEquals("user456", response.getUserId());
        
        verify(chatSessionRepository).findBySessionId("non-existent-session");
        verify(chatSessionRepository).save(any(ChatSession.class));
        verify(openAiService).createChatCompletion(any());
    }

    @Test
    void testSessionSummaryTruncatesLongMessage() {
        // Arrange
        String longMessage = "a".repeat(150);
        testSession.getMessages().get(1).setContent(longMessage);
        
        List<ChatSession> sessions = Arrays.asList(testSession);
        when(chatSessionRepository.findByUserId("user123")).thenReturn(sessions);

        // Act
        List<SessionSummary> summaries = chatbotService.getUserSessionSummaries("user123");

        // Assert
        SessionSummary summary = summaries.get(0);
        assertTrue(summary.getLastMessage().length() <= 103); // 100 + "..."
        assertTrue(summary.getLastMessage().endsWith("..."));
    }
}
