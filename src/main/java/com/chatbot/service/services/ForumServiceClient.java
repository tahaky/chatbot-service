package com.chatbot.service.services;

import com.chatbot.service.dto.forum.ForumMessageResponse;
import com.chatbot.service.dto.forum.ForumSubthreadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForumServiceClient {

    private final RestTemplate restTemplate;

    @Value("${forum.service.base-url}")
    private String baseUrl;

    public List<ForumMessageResponse> getSubthreadMessages(String subthreadId) {
        try {
            String url = baseUrl + "/subthreads/" + subthreadId + "/messages";
            List<ForumMessageResponse> messages = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ForumMessageResponse>>() {}
            ).getBody();
            return messages != null ? messages : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch messages for subthread {}: {}", subthreadId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ForumSubthreadResponse> getSubthreadsByThread(String threadId) {
        try {
            String url = baseUrl + "/threads/" + threadId + "/subthreads?includeMessages=false";
            List<ForumSubthreadResponse> subthreads = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ForumSubthreadResponse>>() {}
            ).getBody();
            return subthreads != null ? subthreads : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch subthreads for thread {}: {}", threadId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
