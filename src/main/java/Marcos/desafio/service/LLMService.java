package Marcos.desafio.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LLMService {
    private final OpenRouterService openRouterService;
    private final GeminiService geminiService;
    private final MistralService mistralService;
    private final CohereService cohereService;
    private final AI21Service ai21Service;
    private final LLMResponseParser responseParser;

    public LLMService(OpenRouterService openRouterService, GeminiService geminiService,
                      MistralService mistralService, CohereService cohereService, AI21Service ai21Service,
                      LLMResponseParser responseParser) {
        this.openRouterService = openRouterService;
        this.geminiService = geminiService;
        this.mistralService = mistralService;
        this.cohereService = cohereService;
        this.ai21Service = ai21Service;
        this.responseParser = responseParser;
    }

    public Mono<Map<String, Object>> queryAndEvaluate(String question) {
        return getResponsesFromModels(question)
                .flatMap(responses -> {
                    if (responses.isEmpty()) {
                        return Mono.error(new RuntimeException("Nenhuma resposta válida foi obtida dos modelos."));
                    }

                    Map<String, String> extractedResponses = responseParser.extractResponses(responses);

                    String evaluationPrompt = generateEvaluationPrompt(question, extractedResponses);

                    return getEvaluationsFromModels(evaluationPrompt, extractedResponses)
                            .map(evaluations -> {
                                Map<String, Object> processedEvaluations = responseParser.extractEvaluations(evaluations);
                                Map<String, Object> finalResult = new ConcurrentHashMap<>();
                                finalResult.put("responses", extractedResponses);
                                finalResult.put("evaluations", processedEvaluations);
                                return finalResult;
                            });
                });
    }

    private Mono<Map<String, String>> getResponsesFromModels(String question) {
        Map<String, String> results = new ConcurrentHashMap<>();

        return Flux.mergeDelayError(15,
                openRouterService.query(question).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter resposta do OpenRouter: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("OpenRouter", resp)),

                geminiService.query(question).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter resposta do Gemini: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("Gemini", resp)),

                mistralService.query(question).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter resposta do Mistral: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("Mistral", resp)),

                cohereService.query(question).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter resposta do Cohere: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("Cohere", resp)),

                ai21Service.query(question).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter resposta do AI21: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("AI21", resp))
        ).then(Mono.just(results));
    }

    private Mono<Map<String, String>> getEvaluationsFromModels(String evaluationPrompt, Map<String, String> responses) {
        Map<String, String> results = new ConcurrentHashMap<>();
    
        return Flux.mergeDelayError(15,
                openRouterService.query(evaluationPrompt).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter avaliação do OpenRouter: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("OpenRouter", resp)),
                geminiService.query(evaluationPrompt).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter avaliação do Gemini: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("Gemini", resp)),
                mistralService.query(evaluationPrompt).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter avaliação do Mistral: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("Mistral", resp)),
                cohereService.query(evaluationPrompt).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter avaliação do Cohere: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("Cohere", resp)),
                ai21Service.query(evaluationPrompt).timeout(Duration.ofSeconds(15))
                        .onErrorResume(e -> Mono.just("{\"error\":\"Erro ao obter avaliação do AI21: " + e.getMessage() + "\"}"))
                        .doOnNext(resp -> results.put("AI21", resp))
        ).then(Mono.just(results));
    }

    private String generateEvaluationPrompt(String question, Map<String, String> responses) {
        StringBuilder prompt = new StringBuilder("Você é um avaliador imparcial especializado em análise de respostas de Modelos de Linguagem.\n");
        prompt.append("Sua tarefa é avaliar as respostas de diferentes modelos para uma mesma pergunta com base nos seguintes critérios:\n")
              .append("1. Clareza e coerência (nota de 0 a 10)\n")
              .append("2. Precisão da informação (nota de 0 a 10)\n")
              .append("3. Criatividade ou profundidade (nota de 0 a 10)\n")
              .append("4. Consistência gramatical (nota de 0 a 10)\n\n");

        prompt.append("### Pergunta original: \n").append(question).append("\n\n");
        prompt.append("### Respostas dos modelos:\n\n");

        responses.forEach((model, response) -> 
            prompt.append("**").append(model).append(":** \n").append(response).append("\n\n"));

        prompt.append("Agora, siga as instruções abaixo para gerar sua avaliação:\n\n")
              .append("1. **Atribua uma nota de 0 a 10** para cada modelo nos critérios definidos.\n")
              .append("2. **Calcule a média das notas** para cada modelo.\n")
              .append("3. **Monte um ranking ordenado pela média das notas**.\n\n");

        prompt.append("Apenas retorne um JSON exatamente no seguinte formato:\n");
        prompt.append("{\n")
              .append("  \"evaluations\": {\n")
              .append("    \"ModeloX\": { \"clarity\": 9.0, \"accuracy\": 8.5, \"creativity\": 7.8, \"grammar\": 9.2, \"average\": 8.6 },\n")
              .append("  },\n")
              .append("  \"ranking\": [\n")
              .append("    { \"position\": 1, \"model\": \"ModeloX\", \"average\": 8.6 }\n")
              .append("  ]\n")
              .append("}\n");

        return prompt.toString();
    }
}
