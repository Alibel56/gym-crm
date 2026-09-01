package com.practice.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cucumber.support.ScenarioContext;
import com.practice.dto.request.TraineeRegistrationRequest;
import com.practice.dto.request.TrainerRegistrationRequest;
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


public class RegistrationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioContext scenarioContext;

    private TraineeRegistrationRequest traineeRequest;
    private TrainerRegistrationRequest trainerRequest;

    @Given("I have trainee registration details with first name {string} and last name {string}")
    public void iHaveTraineeRegistrationDetails(String firstName, String lastName) {
        traineeRequest = new TraineeRegistrationRequest(firstName, lastName, null, null);
    }

    @Given("I have trainer registration details with first name {string} and last name {string} and specialization id {long}")
    public void iHaveTrainerRegistrationDetails(String firstName, String lastName, long specializationId) {
        trainerRequest = new TrainerRegistrationRequest(firstName, lastName, specializationId);
    }

    @When("I submit the trainee registration request")
    public void iSubmitTheTraineeRegistrationRequest() throws Exception {
        scenarioContext.setLastResult(mockMvc.perform(post("/api/auth/trainee/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(traineeRequest))));
    }

    @When("I submit the trainer registration request")
    public void iSubmitTheTrainerRegistrationRequest() throws Exception {
        scenarioContext.setLastResult(mockMvc.perform(post("/api/auth/trainer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainerRequest))));
    }

    @Then("the registration response status should be {int}")
    public void theRegistrationResponseStatusShouldBe(int expectedStatus) throws Exception {
        scenarioContext.getLastResult().andExpect(status().is(expectedStatus));
    }

    @Then("the response should contain a generated username and password")
    public void theResponseShouldContainGeneratedCredentials() throws Exception {
        scenarioContext.getLastResult()
                .andExpect(jsonPath("$.userName", not(emptyOrNullString())))
                .andExpect(jsonPath("$.password", not(emptyOrNullString())));
    }
}
