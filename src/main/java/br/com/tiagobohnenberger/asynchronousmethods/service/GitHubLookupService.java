package br.com.tiagobohnenberger.asynchronousmethods.service;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubApiTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubLookupService {

    private final WebClient gitHubWebClient;

    @Async
    public CompletableFuture<User> findUser(String user) throws InterruptedException {
        log.info("Looking up " + user);
        Mono<User> mono = gitHubWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{user}").build(Map.of("user", user)))
                .retrieve()
                .bodyToMono(User.class);
        doSleep();
        return CompletableFuture.supplyAsync(mono::block);
    }

    public GitHubUserResponseDTO getUser(String userName) {
        try {
            User user = this.findUser(userName).get(2000, TimeUnit.MILLISECONDS);
            return new GitHubUserResponseDTO(user);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException ex) {
            String msg = "Timeout error when fetching user data from GitHub API";
            log.warn(msg, ex);
            throw new GitHubApiTimeoutException(msg, ex);
        }
    }

    private static void doSleep() {
        try {
            TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(3, 6));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
