package br.com.tiagobohnenberger.asynchronousmethods.service;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.model.User;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubApiTimeoutException;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.ResourceNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private GitHubLookupService gitHubLookupService;

    @Mock
    private CompletableFuture<User> mockFuture;

    private String existingUserName, nonExistingUserName;
    private User existingUser, nonExistingUser;
    private List<String> userNames;

    @BeforeEach
    void setUp() {
        existingUserName = "PivotalSoftware";
        nonExistingUserName = "asd";
        existingUser = new User("Pivotal Software, Inc.", "http://pivotal.io");
        nonExistingUser = new User(null, null);
        userNames = List.of("PivotalSoftware", "Spring-Projects");

        // ==== CompletableFuture stubbing ====
        lenient().when(gitHubLookupService.findUserFuture(existingUserName))
                .thenReturn(CompletableFuture.completedFuture(existingUser));
        lenient().when(gitHubLookupService.findUserFuture(nonExistingUserName))
                .thenReturn(CompletableFuture.completedFuture(nonExistingUser));

        // ==== Mono stubbing ====
        lenient().when(gitHubLookupService.findUserMono(existingUserName))
                .thenReturn(Mono.just(existingUser));
        lenient().when(gitHubLookupService.findUserMono(nonExistingUserName))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should return DTO when username exists - Future")
    void shouldReturnDTOWhenUsernameExists() {
        GitHubUserResponseDTO dto = userService.getByNameFuture(existingUserName);

        assertEquals("Pivotal Software, Inc.", dto.name());
        assertEquals("http://pivotal.io", dto.blog());
    }

    @Test
    @DisplayName("When username doesn't exists should return DTO with null values")
    void whenUserNameDoesNotExistsShouldReturnDTOWithNullValues() {
        GitHubUserResponseDTO dto = userService.getByNameFuture(nonExistingUserName);

        assertNull(dto.name());
        assertNull(dto.blog());
    }

    @Test
    @DisplayName("When waiting more than 2 seconds, should throw GitHubApiTimeoutException")
    void whenWaitingMoreThan2SecondsShouldThrowTimeoutException() throws ExecutionException, InterruptedException, TimeoutException {
        when(gitHubLookupService.findUserFuture(anyString())).thenReturn(mockFuture);
        when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenThrow(TimeoutException.class);

        GitHubApiTimeoutException timeoutException =
                assertThrowsExactly(GitHubApiTimeoutException.class, () -> userService.getByNameFuture(nonExistingUserName));

        assertEquals("Timeout on looking up user", timeoutException.getLocalizedMessage());
    }

    @Test
    @DisplayName("Should return DTO when username exists - Reactor")
    void shouldReturnDTOWhenUsernameExistsMono() {
        GitHubUserResponseDTO dto = userService.getByNameMono(existingUserName);

        assertEquals("Pivotal Software, Inc.", dto.name());
        assertEquals("http://pivotal.io", dto.blog());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found - Reactor")
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        ResourceNotFoundException resourceNotFoundException =
                assertThrowsExactly(ResourceNotFoundException.class,
                        () -> userService.getByNameMono(nonExistingUserName));

        assertEquals("User asd not found", resourceNotFoundException.getLocalizedMessage());
    }
}
