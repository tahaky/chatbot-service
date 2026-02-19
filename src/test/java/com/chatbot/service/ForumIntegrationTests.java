package com.chatbot.service;

import com.chatbot.service.dto.ChatRequest;
import com.chatbot.service.dto.ChatResponse;
import com.chatbot.service.dto.forum.ForumMessageResponse;
import com.chatbot.service.dto.forum.ForumSubthreadResponse;
import com.chatbot.service.model.ChatSession;
import com.chatbot.service.repository.ChatSessionRepository;
import com.chatbot.service.services.ChatbotService;
import com.chatbot.service.services.ForumServiceClient;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "openai.api-key=test-key",
    "spring.data.mongodb.uri=mongodb://localhost:27017/test",
    "forum.service.base-url=http://localhost:9999"
})
class ForumIntegrationTests {

    @Autowired
    private ChatbotService chatbotService;

    @MockBean
    private ChatSessionRepository chatSessionRepository;

    @MockBean
    private OpenAiService openAiService;

    @MockBean
    private ForumServiceClient forumServiceClient;

    private ChatCompletionResult mockCompletionResult;

    @BeforeEach
    void setUp() {
        mockCompletionResult = mock(ChatCompletionResult.class);
        ChatCompletionChoice mockChoice = mock(ChatCompletionChoice.class);
        com.theokanning.openai.completion.chat.ChatMessage mockMsg =
            new com.theokanning.openai.completion.chat.ChatMessage();
        mockMsg.setContent("Test response");
        when(mockChoice.getMessage()).thenReturn(mockMsg);
        when(mockCompletionResult.getChoices()).thenReturn(Arrays.asList(mockChoice));
        when(openAiService.createChatCompletion(any())).thenReturn(mockCompletionResult);
        when(chatSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testBuildForumContextWithVotedMessagesOnly() {
        ForumMessageResponse msg1 = new ForumMessageResponse();
        msg1.setBody("Great point!");
        msg1.setUpvoteCount(5);
        msg1.setDeleted(false);

        ForumMessageResponse msg2 = new ForumMessageResponse();
        msg2.setBody("Not so great");
        msg2.setUpvoteCount(0);
        msg2.setDeleted(false);

        when(forumServiceClient.getSubthreadMessages("sub-1")).thenReturn(Arrays.asList(msg1, msg2));

        String context = chatbotService.buildForumContext("sub-1", null);

        assertTrue(context.contains("Forum discussion context:"));
        assertTrue(context.contains("Great point!"));
        assertTrue(context.contains("upvotes: 5"));
        assertFalse(context.contains("Not so great"));
    }

    @Test
    void testBuildForumContextIncludesInitialMessageWhenThreadIdProvided() {
        ForumSubthreadResponse subthread = new ForumSubthreadResponse();
        subthread.setId("sub-1");
        subthread.setInitialMessage("This is the initial message of the discussion.");

        ForumMessageResponse msg = new ForumMessageResponse();
        msg.setBody("Upvoted message");
        msg.setUpvoteCount(3);
        msg.setDeleted(false);

        when(forumServiceClient.getSubthreadsByThread("thread-1")).thenReturn(Arrays.asList(subthread));
        when(forumServiceClient.getSubthreadMessages("sub-1")).thenReturn(Arrays.asList(msg));

        String context = chatbotService.buildForumContext("sub-1", "thread-1");

        assertTrue(context.contains("Initial message: This is the initial message of the discussion."));
        assertTrue(context.contains("Upvoted message"));
    }

    @Test
    void testBuildForumContextHandlesEmptyMessages() {
        when(forumServiceClient.getSubthreadMessages("sub-empty")).thenReturn(new ArrayList<>());

        String context = chatbotService.buildForumContext("sub-empty", null);

        assertTrue(context.contains("Forum discussion context:"));
        assertFalse(context.contains("Top voted messages:"));
    }

    @Test
    void testSendMessageWithSubthreadIdEnrichesInitialPrompt() {
        ForumMessageResponse votedMsg = new ForumMessageResponse();
        votedMsg.setBody("This is very helpful");
        votedMsg.setUpvoteCount(10);
        votedMsg.setDeleted(false);

        when(forumServiceClient.getSubthreadMessages("sub-1")).thenReturn(Arrays.asList(votedMsg));

        ChatRequest request = new ChatRequest();
        request.setUserId("user1");
        request.setMessage("Hello");
        request.setSubthreadId("sub-1");

        ChatResponse response = chatbotService.sendMessage(request);

        assertNotNull(response);
        assertNotNull(response.getSessionId());
        verify(forumServiceClient).getSubthreadMessages("sub-1");
    }

    @Test
    void testSendMessageWithoutSubthreadIdSkipsForumFetch() {
        ChatRequest request = new ChatRequest();
        request.setUserId("user1");
        request.setMessage("Hello");

        chatbotService.sendMessage(request);

        verifyNoInteractions(forumServiceClient);
    }

    @Test
    void testSendMessageWithExistingSessionSkipsForumFetch() {
        ChatSession existingSession = new ChatSession();
        existingSession.setSessionId("existing-session");
        existingSession.setUserId("user1");
        existingSession.setMessages(new ArrayList<>());
        existingSession.setInitialPrompt("Existing prompt");
        existingSession.setCreatedAt(LocalDateTime.now());
        existingSession.setUpdatedAt(LocalDateTime.now());

        when(chatSessionRepository.findBySessionIdAndUserId("existing-session", "user1"))
            .thenReturn(Optional.of(existingSession));

        ChatRequest request = new ChatRequest();
        request.setUserId("user1");
        request.setMessage("Hello");
        request.setSessionId("existing-session");
        request.setSubthreadId("sub-1");

        chatbotService.sendMessage(request);

        verifyNoInteractions(forumServiceClient);
    }
}
