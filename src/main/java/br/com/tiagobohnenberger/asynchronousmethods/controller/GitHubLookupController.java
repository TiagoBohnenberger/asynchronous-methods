package br.com.tiagobohnenberger.asynchronousmethods.controller;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/github-users")
@RequiredArgsConstructor
public class GitHubLookupController {

    private final UserService userService;

    @GetMapping(path = "/{userName}")
    public ResponseEntity<GitHubUserResponseDTO> findByUserNameFuture(@PathVariable(value = "userName") String userName) {
        GitHubUserResponseDTO user = userService.getByNameFuture(userName);
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/v2/{userName}")
    public ResponseEntity<GitHubUserResponseDTO> findByUserNameMono(@PathVariable("userName") String userName) {
        GitHubUserResponseDTO user = userService.getByNameMono(userName);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<GitHubUserResponseDTO>> findAllCompletableFuture(
            @RequestParam(name = "names") List<String> usersName
    ) {
        List<GitHubUserResponseDTO> users = userService.getAllUsingCompletableFuture(usersName);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/v2")
    public ResponseEntity<List<GitHubUserResponseDTO>> findAllReactor(
            @RequestParam(name = "names") List<String> usersName
    ) {
        List<GitHubUserResponseDTO> users = userService.getAllUsingReactor(usersName);
        return ResponseEntity.ok(users);
    }
}
