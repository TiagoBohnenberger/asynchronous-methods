package br.com.tiagobohnenberger.asynchronousmethods;

import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import br.com.tiagobohnenberger.asynchronousmethods.service.GitHubLookupService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner implements CommandLineRunner {

    private final GitHubLookupService gitHubLookupService;

    @Override
    public void run(String... args) throws Exception {

        long start = System.currentTimeMillis();

        CompletableFuture<User> page1 = gitHubLookupService.findUser("PivotalSoftware");
        CompletableFuture<User> page2 = gitHubLookupService.findUser("CloudFoundry");
        CompletableFuture<User> page3 = gitHubLookupService.findUser("Spring-Projects");

        CompletableFuture[] completableFutures = Stream.of(page1, page2, page3)
                .map(future -> future.thenAcceptAsync(user -> log.info("--> " + user)))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();

//        log.info("Elapsed time: " + (System.currentTimeMillis() - start));
//        log.info("--> " + page1.get());
//        log.info("--> " + page2.get());
//        log.info("--> " + page3.get());
    }
}
