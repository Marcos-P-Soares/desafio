package Marcos.desafio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenRouterService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final List<String> models = List.of("openchat/openchat-7b:free", "meta-llama/llama-3.3-70b-instruct:free", "deepseek/deepseek-r1-distill-llama-70b:free");

    public OpenRouterService(@Value("${openrouter.base-url}") String baseUrl,
                             @Value("${openrouter.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<Map<String, String>> getResponsesFromModels(String userInput) {
        List<Mono<Map.Entry<String, String>>> responseMonos = models.stream()
            .map(model -> getLLMResponse(model, userInput)
                .map(response -> Map.entry(model, extractMessage(response)))
            )
            .collect(Collectors.toList());

        return Flux.merge(responseMonos)
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<String> getLLMResponse(String model, String userInput) {
        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userInput))
            ))
            .retrieve()
            .bodyToMono(String.class);
    }

    private String extractMessage(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                return choicesNode.get(0).path("message").path("content").asText("Resposta não disponível.");
            }
        } catch (Exception e) {
            return "Erro ao processar resposta.";
        }
        return "Resposta não disponível.";
    }
}