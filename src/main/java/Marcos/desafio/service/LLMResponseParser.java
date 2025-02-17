package Marcos.desafio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> extractResponses(Map<String, String> rawResponses) {
        Map<String, String> extractedResponses = new HashMap<>();

        rawResponses.forEach((model, responseJson) -> {
            try {
                System.out.println("JSON recebido do modelo " + model + ": " + responseJson);
                String extractedText = parseResponse(model, responseJson);
                extractedResponses.put(model, extractedText);
            } catch (Exception e) {
                extractedResponses.put(model, "Erro ao processar resposta do modelo " + model + ": " + e.getMessage());
                System.err.println("Erro ao processar resposta do modelo " + model + ": " + e.getMessage());
            }
        });

        return extractedResponses;
    }

    private String parseResponse(String model, String responseJson) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseJson);

        switch (model) {
            case "Mistral":
            case "AI21":
            case "OpenRouter":
                if (rootNode.has("choices") && rootNode.path("choices").has(0)) {
                    return rootNode.path("choices").get(0).path("message").path("content").asText();
                }
                break;
            case "Cohere":
                if (rootNode.has("message") && rootNode.path("message").has("content")) {
                    return rootNode.path("message").path("content").get(0).path("text").asText();
                }
                break;
            case "Gemini":
                if (rootNode.has("candidates") && rootNode.path("candidates").has(0)) {
                    return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                }
                break;
            default:
                throw new Exception("Modelo desconhecido: " + model);
        }
        throw new Exception("Formato de resposta inesperado para " + model);
    }

    public Map<String, Object> extractEvaluations(Map<String, String> rawEvaluations) {
        Map<String, Object> evaluations = new HashMap<>();
        Map<String, Object> rankings = new HashMap<>();

        rawEvaluations.forEach((model, responseJson) -> {
            try {
                
                String rawExtractedResponse = parseResponse(model, responseJson);

                // Se for AI21, corrigir o formato do ranking antes de passar adiante
                if ("AI21".equals(model)) {
                    rawExtractedResponse = fixAI21RankingFormat(rawExtractedResponse);
                }

                String jsonText = extractJsonFromResponse(rawExtractedResponse);
                JsonNode rootNode = objectMapper.readTree(jsonText);

                if (rootNode.has("evaluations")) {
                    evaluations.put(model, objectMapper.convertValue(rootNode.get("evaluations"), Map.class));
                }
                if (rootNode.has("ranking")) {
                    rankings.put(model, processRanking(rootNode.get("ranking"), model));
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar avaliação do modelo " + model + ": " + e.getMessage());
                System.err.println("Resposta bruta do modelo " + model + ": " + responseJson);
            }
        });

        Map<String, Object> finalEvaluations = new HashMap<>();
        finalEvaluations.put("evaluations", evaluations);
        finalEvaluations.put("rankings", rankings);

        return finalEvaluations;
    }

    private List<Map<String, Object>> processRanking(JsonNode rankingNode, String model) {
        List<Map<String, Object>> fixedRanking = new ArrayList<>();

        try {
            if (rankingNode.isArray()) {
                return objectMapper.convertValue(rankingNode, List.class);
            } else {
                
                rankingNode.fields().forEachRemaining(entry -> {
                    if (entry.getValue().isObject()) {
                        try {
                            Map<String, Object> rankingEntry = objectMapper.convertValue(entry.getValue(), Map.class);
                            fixedRanking.add(rankingEntry);
                        } catch (Exception e) {
                            System.err.println("Erro ao corrigir ranking do modelo " + model + ": " + e.getMessage());
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar ranking do modelo " + model + ": " + e.getMessage());
        }

        if (fixedRanking.isEmpty()) {
            System.err.println("Ranking do modelo " + model + " está em um formato inválido e não pôde ser corrigido.");
        }

        return fixedRanking;
    }

    private String extractJsonFromResponse(String response) {

        if (response.contains("\"error\"")) {
            return "{\"error\": \"A API retornou um erro na resposta\"}";
        }
    
        // Remove delimitadores de código (` ```json `)
        Pattern pattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            response = matcher.group(1);
        }
    
        response = response.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");
        // **Correção específica para o AI21**

    
        return response.trim();
    }

    private String fixAI21RankingFormat(String response) {
        // Verifica se o ranking do AI21 está no formato incorreto
        if (!response.contains("\"ranking\": [") || !response.contains("\"AI21\": {")) {
            return response; // Se não contém o problema, retorna sem modificar
        }
    
        System.out.println("Corrigindo ranking do AI21...");
    
        // Expressão regular para encontrar a seção de ranking e capturar os elementos incorretos
        Pattern pattern = Pattern.compile("\"ranking\": \\[(.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
    
        if (matcher.find()) {
            String rankingContent = matcher.group(1); // Captura apenas o conteúdo dentro de "ranking": [...]
    
            // Corrige o problema removendo as chaves nomeadas e deixando apenas os objetos
            String fixedRanking = rankingContent.replaceAll("\"[A-Za-z0-9]+\": \\{", "{");
    
            // Monta a resposta corrigida substituindo apenas a parte do ranking
            response = response.replace(rankingContent, fixedRanking);
    
            System.out.println("Ranking do AI21 corrigido!");
        }
    
        return response;
    }
    
}
