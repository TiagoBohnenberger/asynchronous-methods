package br.com.tiagobohnenberger.asynchronousmethods.service;

import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import static java.util.Objects.isNull;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
class GitHubLookupService {

    private final WebClient gitHubWebClient;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Async
    CompletableFuture<User> findUserFuture(String userName) {
        assertValid(userName);
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/users/" + userName))
                .GET()
                .build();

        log.info("Looking up " + userName);
        return httpClient
                .sendAsync(request, BodyHandlers.ofString())
                .thenApply((HttpResponse<String> response) -> {
                    try {
                        return mapper.readValue(response.body(), User.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    Mono<User> findUserMono(String userName) {
        assertValid(userName);
        log.info("Looking up " + userName);
        return gitHubWebClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/{user}")
                                .build(Map.of("user", userName)))
                .retrieve()
                .bodyToMono(User.class);
    }

    private static void assertValid(String user) {
        if (isNull(user) || user.isBlank()) {
            throw new IllegalArgumentException("Username must be provided");
        }
    }
}
