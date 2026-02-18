package com.chatbot.service.repository;

import com.chatbot.service.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    
    Optional<ChatSession> findBySessionId(String sessionId);
    
    List<ChatSession> findByUserId(String userId);
    
    Optional<ChatSession> findBySessionIdAndUserId(String sessionId, String userId);
}
