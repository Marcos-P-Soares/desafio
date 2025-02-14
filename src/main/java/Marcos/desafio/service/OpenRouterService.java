package Marcos.desafio.service;

import Marcos.desafio.config.LLMConfig;
import Marcos.desafio.exception.ApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenRouterService {
    private final WebClient webClient;
    private final LLMConfig llmConfig;

    public OpenRouterService(WebClient.Builder webClientBuilder, LLMConfig llmConfig) {
        this.webClient = webClientBuilder.baseUrl("https://openrouter.ai/api/v1").build();
        this.llmConfig = llmConfig;
    }

    public Mono<String> query(String question) {
        String apiKey = llmConfig.getApiKey("openrouter");

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "meta-llama/llama-3.3-70b-instruct:free",
                        "messages", new Object[]{Map.of("role", "user", "content", question)}
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> 
                        Mono.defer(() -> Mono.error(new ApiException("Erro do cliente na API OpenRouter: " + response.statusCode(), response.statusCode()))))
                .onStatus(HttpStatusCode::is5xxServerError, response -> 
                        Mono.defer(() -> Mono.error(new ApiException("Erro do servidor na API OpenRouter: " + response.statusCode(), response.statusCode()))))
                .bodyToMono(String.class);
    }
}
