package br.com.tiagobohnenberger.asynchronousmethods.controller.advice;

import java.time.Instant;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public final class StandardError {
    private final HttpStatus status;
    private final String message;
    private final String requestPath;
    private final Instant instant;

    public StandardError(HttpStatus status, String message, String requestPath) {
        this.status = status;
        this.message = message;
        this.requestPath = requestPath;
        this.instant = Instant.now();
    }
}
