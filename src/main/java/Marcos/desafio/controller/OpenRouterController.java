package Marcos.desafio.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Marcos.desafio.service.OpenRouterService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/openrouter")
public class OpenRouterController {
     private final OpenRouterService openRouterService;

    public OpenRouterController(OpenRouterService openRouterService) {
        this.openRouterService = openRouterService;
    }

    @PostMapping("/ask-multiple")
    public Mono<ResponseEntity<Map<String, String>>> askMultipleModels(@RequestBody String question) {
        return openRouterService.getResponsesFromModels(question)
                .map(ResponseEntity::ok);
    }
}
