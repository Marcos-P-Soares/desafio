package Marcos.desafio.dto;

public class ModelResponseDTO {
    private String modelName;
    private String response;

    public ModelResponseDTO(String modelName, String response) {
        this.modelName = modelName;
        this.response = response;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
