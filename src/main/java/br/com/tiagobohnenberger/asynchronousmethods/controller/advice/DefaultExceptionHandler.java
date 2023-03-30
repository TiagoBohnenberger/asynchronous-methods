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
        return ResponseEntity
                .status(status)
                .body(standardError);
    }
}
