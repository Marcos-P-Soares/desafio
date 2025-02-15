package Marcos.desafio.service;

import Marcos.desafio.config.LLMConfig;
import Marcos.desafio.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GeminiService {
    private final WebClient webClient;
    private final LLMConfig llmConfig;

    public GeminiService(WebClient.Builder webClientBuilder, LLMConfig llmConfig) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
        this.llmConfig = llmConfig;
    }

    public Mono<String> query(String question) {
        String apiKey = llmConfig.getApiKey("gemini");

            return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/models/gemini-1.5-flash:generateContent")
                    .queryParam("key", apiKey)
                    .build())
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", question)})}
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new ApiException("Erro do cliente na API Gemini: " + clientResponse.statusCode(), HttpStatus.valueOf(clientResponse.statusCode().value()))))  
                .onStatus(HttpStatusCode::is5xxServerError, response -> 
                    Mono.defer(() -> Mono.error(new ApiException("Erro do servidor na API Gemini: " + response.statusCode(), response.statusCode()))))
                .bodyToMono(String.class);
    }
}
