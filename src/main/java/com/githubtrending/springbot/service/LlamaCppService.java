package com.githubtrending.springbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class LlamaCppService {

    private static final Logger log = LoggerFactory.getLogger(LlamaCppService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LlamaCppService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Value("${llamacpp.server.url}")
    private String serverUrl;

    @Value("${llamacpp.api.endpoint:/api/chat}")
    private String apiEndpoint;

    public String generateSummary(String prompt) {
        log.info("Generating summary with llamacpp server");

        // Create chat request body for llamacpp
        Map<String, Object> requestBody = Map.of(
                "model", "llama",
                "prompt", prompt,
                "stream", false,
                "n_predict", 1024,
                "temperature", 0.7,
                "top_p", 0.9,
                "top_k", 40,
                "repeat_penalty", 1.1
        );

        try {
            String jsonResponse = webClient.post()
                    .uri(serverUrl + apiEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.debug("Received response from llamacpp: {}", response))
                    .doOnError(error -> log.error("Error calling llamacpp API", error))
                    .block();

            if (jsonResponse == null) {
                throw new IllegalStateException("Null response from llamacpp server");
            }

            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            String content = extractContentFromResponse(jsonNode);

            log.info("Successfully generated summary, length: {}", content.length());
            return content;

        } catch (Exception e) {
            log.error("Failed to generate summary", e);
            throw new RuntimeException("Failed to generate summary from llamacpp", e);
        }
    }

    private String extractContentFromResponse(JsonNode jsonNode) {
        // Try different response formats that llamacpp might use
        if (jsonNode.has("content")) {
            return jsonNode.get("content").asText();
        }
        if (jsonNode.has("response")) {
            return jsonNode.get("response").asText();
        }
        if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
            return jsonNode.get("message").get("content").asText();
        }
        if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                jsonNode.get("choices").size() > 0) {
            JsonNode firstChoice = jsonNode.get("choices").get(0);
            if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                return firstChoice.get("message").get("content").asText();
            }
            if (firstChoice.has("text")) {
                return firstChoice.get("text").asText();
            }
        }

        // Fallback: return the entire JSON as string if extraction fails
        log.warn("Could not extract content from llamacpp response, returning raw JSON");
        return jsonNode.toString();
    }
}
