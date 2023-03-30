package br.com.tiagobohnenberger.asynchronousmethods.service.exception;

public abstract class GitHubApiException extends RuntimeException {

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
