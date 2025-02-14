package Marcos.desafio.controller;

import Marcos.desafio.dto.QueryRequestDTO;
import Marcos.desafio.service.LLMService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LLMController {
    private final LLMService llmService;

    public LLMController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/query")
    public Mono<ResponseEntity<Map<String, String>>> queryModels(@Valid @RequestBody QueryRequestDTO request) {
        return llmService.queryAll(request.getQuestion())
                .map(responses -> ResponseEntity.ok(responses))
                .onErrorResume(ex -> Mono.just(ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()))));
    }
}
