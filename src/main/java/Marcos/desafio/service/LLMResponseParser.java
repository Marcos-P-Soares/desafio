package Marcos.desafio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
}
