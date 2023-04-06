package br.com.tiagobohnenberger.asynchronousmethods.service;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubApiTimeoutException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final GitHubLookupService gitHubLookupService;

    public GitHubUserResponseDTO getByName(String userName) {
        try {
            CompletableFuture<User> futureUser = gitHubLookupService.findUser(userName);
            return new GitHubUserResponseDTO(futureUser.get(2, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error due fething data from GitHub for user {}", userName);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            log.error("Timeout on looking up user {}", userName);
            throw new GitHubApiTimeoutException("Timeout on looking up user", e);
        }
    }

    public List<GitHubUserResponseDTO> getAll(List<String> usersName) {
        return usersName.parallelStream()
                .map(gitHubLookupService::findUser)
                .map(CompletableFuture::join)
                .map(GitHubUserResponseDTO::new)
                .toList();
    }
}
