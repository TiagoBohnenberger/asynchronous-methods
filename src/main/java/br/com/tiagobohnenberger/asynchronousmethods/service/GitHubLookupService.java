package br.com.tiagobohnenberger.asynchronousmethods.service;

import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import java.util.Map;
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

    @Async
    CompletableFuture<User> findUser(String user) {
        log.info("Looking up " + user);
        Mono<User> mono = gitHubWebClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/{user}")
                                .build(Map.of("user", user)))
                .retrieve()
                .bodyToMono(User.class);
        return CompletableFuture.supplyAsync(mono::block);
    }
}
