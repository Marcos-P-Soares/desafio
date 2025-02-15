package Marcos.desafio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
                // Extrai o JSON da string retornada pelo modelo
                String jsonText = extractJsonFromResponse(parseResponse(model, responseJson));

                JsonNode rootNode = objectMapper.readTree(jsonText);

                if (rootNode.has("evaluations")) {
                    evaluations.put(model, objectMapper.convertValue(rootNode.get("evaluations"), Map.class));
                }
                if (rootNode.has("ranking")) {
                    rankings.put(model, objectMapper.convertValue(rootNode.get("ranking"), Object.class));
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar avaliação do modelo " + model + ": " + e.getMessage());
            }
        });

        Map<String, Object> finalEvaluations = new HashMap<>();
        finalEvaluations.put("evaluations", evaluations);
        finalEvaluations.put("rankings", rankings);

        return finalEvaluations;
    }

    private String extractJsonFromResponse(String response) {
        // Remove delimitadores de código (` ```json `)
        Pattern pattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Se não encontrar os delimitadores, tenta processar a resposta diretamente
        return response.trim();
    }
}
