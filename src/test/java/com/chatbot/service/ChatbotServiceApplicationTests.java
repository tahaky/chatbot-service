package com.chatbot.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "openai.api-key=test-key",
    "spring.data.mongodb.uri=mongodb://localhost:27017/test"
})
class ChatbotServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}
