package com.villo.domain.todo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villo.domain.todo.dto.TodoAiRequest;
import com.villo.domain.todo.dto.TodoAiResultResponse;
import com.villo.domain.todo.entity.type.Difficulty;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class TodoAiService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public TodoAiService(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.model}") String model,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    // Groq API 호출 → 카테고리/난이도/골드 분석
    public TodoAiResultResponse analyze(String todoTitle) {
        try {
            String responseBody = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(TodoAiRequest.of(model, todoTitle))
                    .retrieve()
                    .body(String.class);

            return parseResponse(responseBody);

        } catch (Exception e) {
            log.error("Groq API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    // Groq 응답에서 JSON 추출 및 파싱
    private TodoAiResultResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String text = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();

            String cleanJson = text
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode result = objectMapper.readTree(cleanJson);

            return new TodoAiResultResponse(
                    result.path("category").asText("생활"),
                    Difficulty.valueOf(result.path("difficulty").asText("NORMAL")),
                    result.path("gold").asInt(50)
            );

        } catch (Exception e) {
            log.error("Groq 응답 파싱 실패: {}", e.getMessage());
            return new TodoAiResultResponse("생활", Difficulty.NORMAL, 50);
        }
    }
}
