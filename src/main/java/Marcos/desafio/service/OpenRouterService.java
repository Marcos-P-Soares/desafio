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

@Service
public class OpenRouterService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final List<String> models = List.of(
        "google/gemini-2.0-flash-lite-preview-02-05:free",
        "nvidia/llama-3.1-nemotron-70b-instruct:free",
        "meta-llama/llama-3.3-70b-instruct:free"
    );

    public OpenRouterService(@Value("${openrouter_base_url}") String baseUrl,
                             @Value("${openrouter_api_key}") String apiKey) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<Map<String, Object>> getResponsesAndRankings(String userInput) {
        return getResponsesFromModels(userInput)
            .flatMap(responses -> rankResponsesByEachModel(userInput, responses)
                .map(rankings -> Map.of("responses", responses, "rankings", rankings))
            );
    }

    private Mono<Map<String, String>> getResponsesFromModels(String userInput) {
        return Flux.fromIterable(models)
            .flatMap(model -> fetchResponse(model, userInput))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map.Entry<String, String>> fetchResponse(String model, String userInput) {
        return getLLMResponse(model, userInput)
            .map(response -> Map.entry(model, extractMessage(response)));
    }

    private Mono<Map<String, String>> rankResponsesByEachModel(String userPrompt, Map<String, String> responses) {
        return Flux.fromIterable(models)
            .flatMap(model -> fetchRanking(model, userPrompt, responses))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<Map.Entry<String, String>> fetchRanking(String model, String userPrompt, Map<String, String> responses) {
        return getLLMResponse(model, buildRankingPrompt(userPrompt, responses))
            .map(response -> Map.entry(model, extractRanking(response)));
    }

    private String buildRankingPrompt(String userPrompt, Map<String, String> responses) {
        StringBuilder prompt = new StringBuilder(
            String.format("Aqui estão as respostas de diferentes modelos para a entrada: \"%s\"\n\n", userPrompt)
        );

        responses.forEach((model, response) ->
            prompt.append(String.format("Modelo: %s\nResposta: %s\n\n", model, response))
        );

        prompt.append("Avalie cada resposta de 0 a 10 nos seguintes critérios:\n");
        prompt.append("- Clareza e coerência da resposta\n");
        prompt.append("- Precisão da informação\n");
        prompt.append("- Criatividade e profundidade da resposta\n");
        prompt.append("- Consistência gramatical\n\n");
        prompt.append("Retorne a resposta seguindo a estrutura:\n");
        prompt.append("{\n");
        prompt.append("  \"(nome do modelo, Exemplo: 'Gemini')\": \"(nota para cada um dos critérios) Exemplo: clareza: 8, Precisão da informação: 9, Criatividade: 7, Consistência gramatical: 8\",\n");
        prompt.append("}\n\n");

        prompt.append("Agora calcule a média das notas e me retorne a seguinte estrutura de ranking com base nas médias:\n\n");
        prompt.append("{\n");
        prompt.append("  \"(posição) - (nome do modelo)\": \"(média)\",\n");
        prompt.append("Exemplo: 1 - GPT: 8.5 \n");
        prompt.append("}\n\n");
        prompt.append("IMPORTANTE: Retorne **apenas** o ranking seguindo a estrutura, sem explicações ou comentários adicionais.\n");

        return prompt.toString();
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

    private String extractRanking(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode choicesNode = rootNode.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                return choicesNode.get(0).path("message").path("content").asText("Ranking não disponível.");
            }
        } catch (Exception e) {
            return "Erro ao processar ranking.";
        }
        return "Ranking não disponível.";
    }
}