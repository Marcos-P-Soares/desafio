package Marcos.desafio.service;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LLMService {
    private final OpenRouterService openRouterService;
    private final GeminiService geminiService;
    private final MistralService mistralService;
    private final CohereService cohereService;
    private final AI21Service ai21Service;

    public LLMService(OpenRouterService openRouterService, GeminiService geminiService,
                      MistralService mistralService, CohereService cohereService, AI21Service ai21Service) {
        this.openRouterService = openRouterService;
        this.geminiService = geminiService;
        this.mistralService = mistralService;
        this.cohereService = cohereService;
        this.ai21Service = ai21Service;
    }

    public Mono<Map<String, String>> queryAll(String question) {
        Map<String, String> results = new ConcurrentHashMap<>();

        return Flux.merge(
                        openRouterService.query(question)
                                        .map(String::valueOf)
                                        .onErrorResume(e -> Mono.just("Erro ao obter resposta do OpenRouter: " + e.getMessage()))
                                        .doOnNext(response -> results.put("OpenRouter", response)),
                        geminiService.query(question)
                                        .map(String::valueOf)
                                        .onErrorResume(e -> Mono.just("Erro ao obter resposta do Gemini: " + e.getMessage()))
                                        .doOnNext(response -> results.put("Gemini", response)),
                        mistralService.query(question)
                                        .map(String::valueOf)
                                        .onErrorResume(e -> Mono.just("Erro ao obter resposta do Mistral: " + e.getMessage()))
                                        .doOnNext(response -> results.put("Mistral", response)),
                        cohereService.query(question)
                                        .map(String::valueOf)
                                        .onErrorResume(e -> Mono.just("Erro ao obter resposta do Cohere: " + e.getMessage()))
                                        .doOnNext(response -> results.put("Cohere", response)),
                        ai21Service.query(question)
                                        .map(String::valueOf)
                                        .onErrorResume(e -> Mono.just("Erro ao obter resposta do AI21: " + e.getMessage()))
                                        .doOnNext(response -> results.put("AI21", response))
        ).then(Mono.just(results));
    }
}
