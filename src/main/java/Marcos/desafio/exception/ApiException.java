package Marcos.desafio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // Construtor adicional para aceitar HttpStatusCode
    public ApiException(String message, HttpStatusCode statusCode) {
        super(message);
        this.status = HttpStatus.valueOf(statusCode.value());
    }
}
