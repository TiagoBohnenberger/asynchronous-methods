package br.com.tiagobohnenberger.asynchronousmethods.service.exception;

import java.util.concurrent.TimeoutException;

public class GitHubApiTimeoutException extends GitHubApiException {

    public GitHubApiTimeoutException(String message, TimeoutException ex) {
        super(message, ex);
    }
}
