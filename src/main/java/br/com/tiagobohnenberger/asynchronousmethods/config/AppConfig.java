package br.com.tiagobohnenberger.asynchronousmethods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean(name = "gitHubWebClient")
    WebClient gitHubWebClient() {
        return WebClient.create("https://api.github.com/users");
    }
}
