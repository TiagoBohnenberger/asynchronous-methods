package br.com.tiagobohnenberger.asynchronousmethods.config;

import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubForbiddenException;
import static java.util.function.Predicate.isEqual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean("gitHubWebClient")
    WebClient gitHubWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.github.com/users")
                .defaultStatusHandler(
                        isEqual(HttpStatus.FORBIDDEN),
                        clientResponse ->
                                Mono.error(new GitHubForbiddenException("To many requests to GitHub API")))
                .build();
    }
}
