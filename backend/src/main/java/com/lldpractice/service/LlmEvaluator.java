package com.lldpractice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lldpractice.domain.Evaluation;
import com.lldpractice.domain.Feedback;
import com.lldpractice.domain.Submission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LlmEvaluator implements Evaluator {

    private final ObjectMapper objectMapper;
    private final String apiKey;

    public LlmEvaluator(ObjectMapper objectMapper, @Value("${llm.api.key:${LLM_API_KEY:}}") String apiKey) {
        this.objectMapper = objectMapper;
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("LLM_API_KEY", System.getenv("LLM_API_KEY"));
        }
        this.apiKey = apiKey;
    }

    @Override
    public Evaluation evaluate(Submission submission, String problemRequirements) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LLM_API_KEY is not set");
        }

        String systemPrompt = """
            You are an expert software architect evaluating a Low-Level Design (LLD) submission.
            Evaluate the submission based on these criteria:
            - requirement_understanding
            - class_responsibilities
            - coupling_cohesion
            - encapsulation_interfaces
            - abstraction_patterns
            - extensibility
            - edge_cases
            - explanation_quality
            
            Return ONLY a valid JSON object matching this exact schema:
            {
              "criteria": [
                {
                  "criterion": "criterion_name",
                  "score": integer (1-10),
                  "evidence": "specific reference to something in the submission",
                  "concern": "specific weakness, or null",
                  "suggestion": "specific actionable improvement",
                  "confidence": "high | medium | low"
                }
              ]
            }
            Do not include markdown blocks, just the JSON.
            """;

        String userPrompt = "Problem Requirements:\n" + problemRequirements + "\n\nSubmission Content:\n" + submission.getContent();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        int maxRetries = 1;
        int attempts = 0;

        while (attempts <= maxRetries) {
            try {
                Map<String, Object> requestBodyMap = Map.of(
                        "model", "openai/gpt-oss-20b",
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)
                        ),
                        "response_format", Map.of("type", "json_object"),
                        "max_tokens", 1000
                );
                
                String requestBody = objectMapper.writeValueAsString(requestBodyMap);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("API Error: " + response.body());
                }

                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                
                JsonNode resultJson = objectMapper.readTree(content);
                List<Feedback> feedbackList = new ArrayList<>();
                for (JsonNode node : resultJson.path("criteria")) {
                    Feedback f = Feedback.builder()
                            .criterion(node.path("criterion").asText())
                            .score(node.path("score").asInt())
                            .evidence(node.path("evidence").asText())
                            .concern(node.path("concern").isNull() ? null : node.path("concern").asText())
                            .suggestion(node.path("suggestion").asText())
                            .confidence(node.path("confidence").asText())
                            .build();
                    feedbackList.add(f);
                }
                
                return Evaluation.builder().criteria(feedbackList).build();
            } catch (Exception e) {
                attempts++;
                if (attempts > maxRetries) {
                    throw new RuntimeException("Evaluation failed after retries", e);
                }
            }
        }
        
        throw new RuntimeException("Evaluation failed");
    }
}
