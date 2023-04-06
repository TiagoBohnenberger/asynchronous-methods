package br.com.tiagobohnenberger.asynchronousmethods.service.exception;

public class GitHubForbiddenException extends GitHubApiException {
    public GitHubForbiddenException(String message) {
        super(message);
    }
}
