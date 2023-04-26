package br.com.tiagobohnenberger.asynchronousmethods.controller.advice;

import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubApiTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(GitHubApiTimeoutException.class)
    public ResponseEntity<StandardError> gitHubApiTimeoutException(GitHubApiTimeoutException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FAILED_DEPENDENCY;
        StandardError standardError = new StandardError(status, ex.getLocalizedMessage(), request.getRequestURI());
        return responseEntity(status, standardError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> illegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError standardError = new StandardError(status, ex.getLocalizedMessage(), request.getRequestURI());
        return responseEntity(status, standardError);
    }

    private static ResponseEntity<StandardError> responseEntity(HttpStatus status, StandardError standardError) {
        return ResponseEntity
                .status(status)
                .body(standardError);
    }
}
