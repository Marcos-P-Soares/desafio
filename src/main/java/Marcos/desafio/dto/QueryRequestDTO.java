package Marcos.desafio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QueryRequestDTO {

    @NotBlank(message = "A questão não pode estar vazia.")
    @Size(min = 5, max = 1000, message = "A questão deve ter entre 5 e 1000 caracteres.")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
