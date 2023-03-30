package br.com.tiagobohnenberger.asynchronousmethods.dto;

import br.com.tiagobohnenberger.asynchronousmethods.model.User;

public record GitHubUserResponseDTO(
        String name,
        String blog
) {
    public GitHubUserResponseDTO(User user) {
        this(user.getName(), user.getBlog());
    }
}
