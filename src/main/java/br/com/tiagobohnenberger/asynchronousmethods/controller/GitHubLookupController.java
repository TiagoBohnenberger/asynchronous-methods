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
    public ResponseEntity<GitHubUserResponseDTO> findByUserName(@PathVariable("userName") String userName) {
        GitHubUserResponseDTO user = userService.getByName(userName);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<GitHubUserResponseDTO>> findAllByUserName(
            @RequestParam(name = "names") List<String> usersName
    ) {
        List<GitHubUserResponseDTO> users = userService.getAll(usersName);
        return ResponseEntity.ok(users);
    }
}
