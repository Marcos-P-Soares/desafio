package Marcos.desafio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${openrouter_api_key}")
    private String openRouterApiKey;

    @Value("${gemini_api_key}")
    private String geminiApiKey;

    @Value("${mistral_api_key}")
    private String mistralApiKey;

    @Value("${cohere_api_key}")
    private String cohereApiKey;

    @Value("${ai21_api_key}")
    private String ai21ApiKey;

    public String getApiKey(String model) {
        return switch (model) {
            case "openrouter" -> openRouterApiKey;
            case "gemini" -> geminiApiKey;
            case "mistral" -> mistralApiKey;
            case "cohere" -> cohereApiKey;
            case "ai21" -> ai21ApiKey;
            default -> throw new IllegalArgumentException("Modelo não suportado: " + model);
        };
    }
}
