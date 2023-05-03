package br.com.tiagobohnenberger.asynchronousmethods.service;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubApiTimeoutException;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubForbiddenException;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.ResourceNotFoundException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final GitHubLookupService gitHubLookupService;

    public GitHubUserResponseDTO getByNameFuture(String userName) {
        try {
            CompletableFuture<User> futureUser = gitHubLookupService.findUserFuture(userName);
            return new GitHubUserResponseDTO(futureUser.get(2, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error due fetching data from GitHub for user {}", userName);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            log.error("Timeout on looking up user {}", userName);
            throw new GitHubApiTimeoutException("Timeout on looking up user", e);
        }
    }

    public GitHubUserResponseDTO getByNameMono(String userName) {
        User user = gitHubLookupService.findUserMono(userName)
                .blockOptional(Duration.ofSeconds(1))
                .orElseThrow(() -> new ResourceNotFoundException("User " + userName + " not found"));
        return new GitHubUserResponseDTO(user);
    }

    public List<GitHubUserResponseDTO> getAllUsingCompletableFuture(List<String> usersName) {
        return usersName.parallelStream()
                .map(gitHubLookupService::findUserFuture)
                .map(CompletableFuture::join)
                .map(GitHubUserResponseDTO::new)
                .toList();
    }

    public List<GitHubUserResponseDTO> getAllUsingReactor(List<String> usersName) {
        return Flux.fromIterable(usersName)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(gitHubLookupService::findUserMono)
                .map(GitHubUserResponseDTO::new)
                .ordered(byName())
                .collectList()
                .onErrorMap(throwable -> {
                    Throwable t = throwable.getSuppressed()[0];
                    if (t instanceof GitHubForbiddenException) {
                        return new GitHubForbiddenException(t.getLocalizedMessage());
                    }
                    return new RuntimeException(throwable.getLocalizedMessage());
                })
                .block();
    }

    private static Comparator<GitHubUserResponseDTO> byName() {
        return Comparator.comparing(GitHubUserResponseDTO::name);
    }
}
