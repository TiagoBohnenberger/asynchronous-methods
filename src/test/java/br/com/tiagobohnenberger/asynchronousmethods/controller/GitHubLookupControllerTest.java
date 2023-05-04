package br.com.tiagobohnenberger.asynchronousmethods.controller;

import br.com.tiagobohnenberger.asynchronousmethods.dto.GitHubUserResponseDTO;
import br.com.tiagobohnenberger.asynchronousmethods.service.UserService;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubApiTimeoutException;
import br.com.tiagobohnenberger.asynchronousmethods.service.exception.GitHubForbiddenException;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MultiValueMapAdapter;

@WebMvcTest(controllers = GitHubLookupController.class)
class GitHubLookupControllerTest {
    static final String CONTROLLER_PATH = "/github-users";
    static final String CONTROLLER_PATH_V2 = "/github-users/v2";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService service;

    String validName;
    List<String> validNames;

    @BeforeEach
    void setUp() {
        validName = "PivotalSoftware";
        validNames = List.of("PivotalSoftware", "CloudFoundry");

        GitHubUserResponseDTO dto = new GitHubUserResponseDTO("Pivotal Software, Inc.", "http://pivotal.io");
        List<GitHubUserResponseDTO> dtoList = Factory.listOfDTOs();
        when(service.getByNameFuture(validName)).thenReturn(dto);
        when(service.getByNameMono(validName)).thenReturn(dto);
        when(service.getAllUsingReactor(validNames)).thenReturn(dtoList);
    }

    @Test
    @DisplayName("Should return DTO and status 200 OK when username is provided")
    void whenUsernameProvidedShouldReturnStatusOk() {
        Assertions.assertDoesNotThrow(() -> {
            ResultActions result =
                    mockMvc.perform(get(CONTROLLER_PATH + "/{userName}", validName)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE),
                    MockMvcResultMatchers.jsonPath("$.name").exists(),
                    MockMvcResultMatchers.jsonPath("$.blog").exists()
            );
        });
    }

    @Test
    @DisplayName("Should return status 424 when waiting for too long")
    void whenToLongShouldReturnTimeOutException() {
        when(service.getByNameFuture(anyString()))
                .thenThrow(GitHubApiTimeoutException.class);

        Assertions.assertDoesNotThrow(() -> {
            ResultActions result =
                    mockMvc.perform(get(CONTROLLER_PATH + "/{userName}", validName)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpectAll(
                    MockMvcResultMatchers.status().isFailedDependency(),
                    MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE),
                    MockMvcResultMatchers.jsonPath("$.status").exists()
            );
        });
    }

    //============== VERSION 2 TESTS API ==============

    @Test
    @DisplayName("Should return DTO and status 200 OK when username is provided v2 API")
    void whenUsernameProvidedShouldReturnStatusOk_v2API() {
        Assertions.assertDoesNotThrow(() -> {
            ResultActions result =
                    mockMvc.perform(get(CONTROLLER_PATH_V2 + "/{userName}", validName)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON));

            result.andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE),
                    MockMvcResultMatchers.jsonPath("$.name").exists(),
                    MockMvcResultMatchers.jsonPath("$.blog").exists()
            );
        });
    }

    @Test
    @DisplayName("Should return List of DTOs and status 200 OK when usernames are provided v2 API")
    void whenUsernamesProvidedShouldReturnStatusOk_v2API() {
        Map<String, List<String>> names = Map.of("names", validNames);

        Assertions.assertDoesNotThrow(() -> {
            ResultActions result =
                    mockMvc.perform(get(CONTROLLER_PATH_V2)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(new MultiValueMapAdapter<>(names)));

            result.andExpectAll(
                    MockMvcResultMatchers.status().isOk(),
                    MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE),
                    MockMvcResultMatchers.jsonPath("$").isArray(),
                    MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)),
                    MockMvcResultMatchers.jsonPath("$[0].name").value("Pivotal Software, Inc."),
                    MockMvcResultMatchers.jsonPath("$[0].blog").value("http://pivotal.io"),
                    MockMvcResultMatchers.jsonPath("$[1].name").value("Cloud Foundry"),
                    MockMvcResultMatchers.jsonPath("$[1].blog").value("https://www.cloudfoundry.org/")
            );
        });
    }

    @Test
    @DisplayName("Should return forbidden status code when to many requests v2 API")
    void whenToManyRequestsShouldReturnStatusCode403Forbidden_v2API() {
        when(service.getAllUsingReactor(anyList()))
                .thenThrow(new GitHubForbiddenException("To many requests to GitHub API"));

        Map<String, List<String>> names = Map.of("names", validNames);

        Assertions.assertDoesNotThrow(() -> {
            ResultActions result =
                    mockMvc.perform(get(CONTROLLER_PATH_V2)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(new MultiValueMapAdapter<>(names)));

            result.andExpectAll(
                    MockMvcResultMatchers.status().isForbidden(),
                    MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE),
                    MockMvcResultMatchers.jsonPath("$.status").value("403 FORBIDDEN"),
                    MockMvcResultMatchers.jsonPath("$.message").value("To many requests to GitHub API"),
                    MockMvcResultMatchers.jsonPath("$.requestPath").value("/github-users/v2"),
                    MockMvcResultMatchers.jsonPath("$.instant").exists()
            );
        });
    }

    private static class Factory {
        private static List<GitHubUserResponseDTO> listOfDTOs() {
            return List.of(
                    new GitHubUserResponseDTO("Pivotal Software, Inc.", "http://pivotal.io"),
                    new GitHubUserResponseDTO("Cloud Foundry", "https://www.cloudfoundry.org/")
            );
        }

    }
}
