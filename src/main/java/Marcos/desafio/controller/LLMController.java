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
    public Mono<ResponseEntity<Map<String, Object>>> queryModels(@Valid @RequestBody QueryRequestDTO request) {
        return llmService.queryAndEvaluate(request.getQuestion())
                .map(responses -> ResponseEntity.ok().body(responses))
                .onErrorResume(ex -> Mono.just(ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()))));
    }

    @GetMapping("/poema-nordeste")
    public Mono<Map<String, Object>> getPoemaNordeste() {
        return llmService.queryAndEvaluate("Escreva um poema sobre a beleza do nordeste brasileiro, usando metáforas e rimas.");
    }

    @GetMapping("/logica-gatos")
    public Mono<Map<String, Object>> getLogicaGatos() {
        return llmService.queryAndEvaluate("Se todos os gatos são mamíferos e todos os mamíferos têm pelos, todos os gatos têm pelos? Explique seu raciocínio.");
    }

    @GetMapping("/primeiro-presidente")
    public Mono<Map<String, Object>> getPrimeiroPresidente() {
        return llmService.queryAndEvaluate("Quem foi o primeiro presidente do Brasil?");
    }

    @GetMapping("/relatividade-simples")
    public Mono<Map<String, Object>> getRelatividadeSimples() {
        return llmService.queryAndEvaluate("Explique a teoria da relatividade geral em termos simples, como se estivesse explicando para uma criança de 10 anos.");
    }

    @GetMapping("/distancia-trens")
    public Mono<Map<String, Object>> getDistanciaTrens() {
        return llmService.queryAndEvaluate("Se um trem viaja a 80 km/h e outro trem viaja a 60 km/h na direção oposta, a que distância estarão um do outro em 2 horas?");
    }

}
