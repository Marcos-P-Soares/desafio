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
public class CohereService {
    private final WebClient webClient;
    private final LLMConfig llmConfig;

    public CohereService(WebClient.Builder webClientBuilder, LLMConfig llmConfig) {
        this.webClient = webClientBuilder.baseUrl("https://api.cohere.com/v2").build();
        this.llmConfig = llmConfig;
    }

    public Mono<String> query(String question) {
        String apiKey = llmConfig.getApiKey("cohere");

        return webClient.post()
                .uri("/chat")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "command-r-plus",
                        "messages", new Object[]{Map.of("role", "user", "content", question)}
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new ApiException("Erro do cliente na API Cohere: " + clientResponse.statusCode(), HttpStatus.valueOf(clientResponse.statusCode().value()))))  
                .onStatus(HttpStatusCode::is5xxServerError, response -> 
                    Mono.defer(() -> Mono.error(new ApiException("Erro do servidor na API Cohere: " + response.statusCode(), response.statusCode()))))
                .bodyToMono(String.class);
                
    }
}
