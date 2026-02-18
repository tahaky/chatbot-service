package com.chatbot.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Chatbot Service API")
                .version("1.0.0")
                .description("OpenAI Integration Service for Chatbot with MongoDB storage")
                .contact(new Contact()
                    .name("Chatbot Service")
                    .email("support@chatbot.com")));
    }
}
