package br.com.tiagobohnenberger.asynchronousmethods.controller;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.service.GitHubLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/github-user")
@RequiredArgsConstructor
public class GitHubLookupController {

    private final GitHubLookupService lookupService;

    @GetMapping(path = "/{userName}")
    public ResponseEntity<GitHubUserResponseDTO> findByUserName(@PathVariable("userName") String userName) {
        GitHubUserResponseDTO user = lookupService.getUser(userName);
        return ResponseEntity.ok(user);
    }
}
