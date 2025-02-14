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
public class AI21Service {
    private final WebClient webClient;
    private final LLMConfig llmConfig;

    public AI21Service(WebClient.Builder webClientBuilder, LLMConfig llmConfig) {
        this.webClient = webClientBuilder.baseUrl("https://api.ai21.com/studio/v1").build();
        this.llmConfig = llmConfig;
    }

    public Mono<String> query(String question) {
        String apiKey = llmConfig.getApiKey("ai21");

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "jamba-1.5-mini",
                        "messages", new Object[]{Map.of("role", "user", "content", question)}
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new ApiException("Erro do cliente na API AI21: " + clientResponse.statusCode(), HttpStatus.valueOf(clientResponse.statusCode().value()))))
                .onStatus(HttpStatusCode::is5xxServerError, response -> 
                    Mono.defer(() -> Mono.error(new ApiException("Erro do servidor na API AI21: " + response.statusCode(), response.statusCode()))))
                .bodyToMono(String.class);
    }
}
