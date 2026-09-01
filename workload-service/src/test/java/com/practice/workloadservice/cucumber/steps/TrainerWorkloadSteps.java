package com.practice.workloadservice.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.workloadservice.cucumber.support.ScenarioContext;
import com.practice.workloadservice.dto.ActionType;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TrainerWorkloadSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioContext scenarioContext;

    private TrainerWorkloadRequest pendingRequest;

    @Given("a workload update for trainer {string} {string} {string} active with {int} minutes of training on {string} of type {string}")
    public void aWorkloadUpdateFor(String username, String firstName, String lastName,
                                    int duration, String date, String actionType) {
        pendingRequest = new TrainerWorkloadRequest(
                username, firstName, lastName, true,
                LocalDate.parse(date), duration, ActionType.valueOf(actionType));
    }

    @When("a workload update for trainer {string} {string} {string} active with {int} minutes of training on {string} of type {string} is submitted")
    public void aWorkloadUpdateIsSubmitted(String username, String firstName, String lastName,
                                           int duration, String date, String actionType) throws Exception {
        aWorkloadUpdateFor(username, firstName, lastName, duration, date, actionType);
        submitWorkloadUpdate();
    }

    @When("I submit the workload update request")
    public void iSubmitTheWorkloadUpdateRequest() throws Exception {
        submitWorkloadUpdate();
    }

    private void submitWorkloadUpdate() throws Exception {
        scenarioContext.setLastResult(mockMvc.perform(post("/api/v1/workloads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pendingRequest))));
    }

    @When("I request the workload summary for trainer {string}")
    public void iRequestTheWorkloadSummaryForTrainer(String username) throws Exception {
        scenarioContext.setLastResult(mockMvc.perform(get("/api/v1/workloads/{username}", username)));
    }

    @Then("the workload update response status should be {int}")
    public void theWorkloadUpdateResponseStatusShouldBe(int expectedStatus) throws Exception {
        scenarioContext.getLastResult().andExpect(status().is(expectedStatus));
    }

    @Then("the workload summary for trainer {string} should show {int} minutes for year {int} month {int}")
    public void theWorkloadSummaryShouldShowMinutes(String username, int minutes, int year, int month) throws Exception {
        mockMvc.perform(get("/api/v1/workloads/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value(username))
                .andExpect(jsonPath("$.years[?(@.year == " + year + ")].months[?(@.month == "
                        + month + ")].trainingSummaryDuration").value(minutes));
    }
}
