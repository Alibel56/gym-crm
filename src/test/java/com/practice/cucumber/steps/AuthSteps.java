package com.practice.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cucumber.support.ScenarioContext;
import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.TraineeRegistrationRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class AuthSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioContext scenarioContext;

    @Given("a trainee {string} {string} is already registered")
    public void aTraineeIsAlreadyRegistered(String firstName, String lastName) throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(firstName, lastName, null, null);

        String responseBody = mockMvc.perform(post("/api/auth/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var node = objectMapper.readTree(responseBody);
        String username = node.get("userName").asText();
        String password = node.get("password").asText();

        String fullName = firstName + " " + lastName;
        scenarioContext.rememberUsernameForFullName(fullName, username);
        scenarioContext.rememberCredentials(username, password);
    }

    @When("{string} {string} logs in with the correct password")
    public void logsInWithCorrectPassword(String firstName, String lastName) throws Exception {
        String username = scenarioContext.usernameForFullName(firstName + " " + lastName);
        String password = scenarioContext.passwordFor(username);
        login(username, password);
    }

    @When("{string} {string} logs in with the password {string}")
    public void logsInWithGivenPassword(String firstName, String lastName, String password) throws Exception {
        String username = scenarioContext.usernameForFullName(firstName + " " + lastName);
        login(username, password);
    }

    @When("user {string} logs in with the password {string}")
    public void userLogsInWithPassword(String username, String password) throws Exception {
        login(username, password);
    }

    private void login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);

        var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        scenarioContext.setLastResult(result);

        String body = result.andReturn().getResponse().getContentAsString();
        if (!body.isBlank()) {
            var node = objectMapper.readTree(body);
            if (node.has("token")) {
                scenarioContext.setIssuedToken(node.get("token").asText());
            }
        }
    }

    @When("{string} {string} logs out")
    public void logsOut(String firstName, String lastName) throws Exception {
        scenarioContext.setLastResult(mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + scenarioContext.getIssuedToken())));
    }

    @Then("the login response status should be {int}")
    public void theLoginResponseStatusShouldBe(int expectedStatus) throws Exception {
        scenarioContext.getLastResult().andExpect(status().is(expectedStatus));
    }

    @Then("the logout response status should be {int}")
    public void theLogoutResponseStatusShouldBe(int expectedStatus) throws Exception {
        scenarioContext.getLastResult().andExpect(status().is(expectedStatus));
    }

    @Then("the response should contain a JWT access token")
    public void theResponseShouldContainAJwtAccessToken() throws Exception {
        scenarioContext.getLastResult().andExpect(jsonPath("$.token", not(emptyOrNullString())));
    }
}
