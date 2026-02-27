package com.jkh1447.MyProject.service.matching.aiMatching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.domain.matching.aiMatching.GameAlias;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.repository.matching.GameAliasRepository;
import java.util.Map;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIMatchingService {

  @Value("${gemini.api.key}")
  private String apiKey;

  private final WebClient geminiWebClient;
  private final ObjectMapper objectMapper;
  private final GameAliasRepository gameAliasRepository;

  public String fetchStandardName(String userInput) {

    String prompt = "Extract the official English game title from: '" + userInput + "'. "
        + "Rules: 1. Uppercase only. 2. Replace space with '_'. 3. Return ONLY JSON: {\"game\": \"NAME\"}";

    Map<String, Object> body = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

    String response = geminiWebClient.post()
            .uri(uriBuilder -> uriBuilder.path("v1beta/models/gemini-2.5-flash-lite:generateContent")
                .queryParam("key", apiKey).build())
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> {
              String fallbackValue = userInput.toUpperCase().replace(" ", "_");
              log.error("[gemini 에러] gemini api error: {}", e.getMessage());
              return Mono.just("{\"game\": \"" + fallbackValue + "\"}");
            })
            .block(); // t2.micro의 쓰레드 관리를 위해 결과를 기다림

    return parseGameName(response);
  }

  private String parseGameName(String response) {
        try {
        JsonNode root = objectMapper.readTree(response);
        
        String targetJson;
        // 1. Gemini 정식 응답(candidates 필드 존재)인 경우
        if (root.has("candidates")) {
            String aiText = root.path("candidates").get(0)
                                .path("content").path("parts").get(0)
                                .path("text").asText();
            // AI가 붙인 마크다운 기호(```json) 제거
            targetJson = aiText.replaceAll("```json|```", "").trim();
        } else {
            // 2. 에러 발생 시 생성한 단순 JSON인 경우
            targetJson = response;
        }

        // 최종적으로 "game" 키의 값만 추출
        JsonNode resultNode = objectMapper.readTree(targetJson);
        return resultNode.get("game").asText().toUpperCase().trim();

    } catch (Exception e) {
        log.error("Final Parsing Error: {}", e.getMessage());
        return "UNKNOWN_GAME";
    }
  }

  public String getStandardNameString(MatchingRequest request) {

    String userInput = request.filters().get(MatchingConstants.AI_USER_INPUT);
    String sanitizedInput = userInput.trim().toLowerCase();

    String standardName = gameAliasRepository.findByUserInput(sanitizedInput)
        .map(GameAlias::getStandardName)
        .orElseGet(() -> {
            String aiResult = fetchStandardName(sanitizedInput);
            gameAliasRepository.save(new GameAlias(sanitizedInput, aiResult));
            return aiResult;
        });
    log.info("[표준 이름] {}", standardName);
      return standardName;
  }

}
