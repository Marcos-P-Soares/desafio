# Desafio - API com Integração LLMs

Este projeto é uma API desenvolvida em **Spring Boot** que se comunica com diversos modelos de IA para processar e avaliar respostas automaticamente.

## Como Executar Localmente

### **Pré-requisitos**
- **Java 17** ou superior
- **Maven**
- **Postman** (opcional, para testes)

### **Passos para Rodar o Projeto**

1. Clone este repositório:
   ```sh
   git clone https://github.com/seu-usuario/desafio.git
   cd desafio
   ```

2. Crie o arquivo `application.properties` dentro do diretório `src/main/resources/` e adicione as seguintes chaves de API:
   ```properties
   openrouter_api_key="chave api openrouter"
   gemini_api_key="chave api gemini"
   mistral_api_key="chave api mistral"
   cohere_api_key="chave api cohere"
   ai21_api_key="chave api ai21"
   ```
   **OBS:** Sem este arquivo, a API não funcionará corretamente.

3. Compile e execute a aplicação com:
   ```sh
   ./mvnw spring-boot:run
   ```

4. A API estará rodando localmente em:
   ```
   http://localhost:8080
   ```

## Testando a API Hospedada no Koyeb

Caso não queira rodar localmente, a API está hospedada no **Koyeb** e pode ser acessada diretamente via:
```
https://hon-aleen-marcos-paulo-453ea20b.koyeb.app
```
Basta adicionar a rota desejada ao final do link.

Exemplo:
```
https://hon-aleen-marcos-paulo-453ea20b.koyeb.app/api/poema-nordeste
```

### **Documentação dos Endpoints**
Os endpoints da API estão documentados em:
```
https://hon-aleen-marcos-paulo-453ea20b.koyeb.app/swagger-ui.html
```

## Obtendo as Chaves de API
Para utilizar esta API, você precisará criar uma conta e obter chaves de API nas seguintes plataformas:

| Provedor  | Link para obter API Key |
|-----------|-------------------------|
| OpenRouter | [Obter chave OpenRouter](https://openrouter.ai/docs/api-reference/authentication) |
| Gemini (Google) | [Obter chave Gemini](https://ai.google.dev/gemini-api/docs/api-key?hl=pt-br) |
| Mistral AI | [Obter chave Mistral](https://console.mistral.ai/api-keys/) |
| Cohere AI | [Obter chave Cohere](https://dashboard.cohere.com/api-keys) |
| AI21 Labs | [Obter chave AI21](https://studio.ai21.com/v2/account/api-key) |


---

## Contato
Caso tenha dúvidas ou precise de suporte, entre em contato via [marcos.paulo.s.m.filho@gmail.com].

