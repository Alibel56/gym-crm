package com.practice.workloadservice.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.workloadservice.messaging.TrainingWorkloadEvent;
import com.practice.workloadservice.messaging.WorkloadActionType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TrainingWorkloadIntegrationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jms.queue.training-workload}")
    private String queueName;

    private TrainingWorkloadEvent pendingEvent;

    @Given("gym-crm records a training of {int} minutes for trainer {string} {string} {string} on {string} with action {string}")
    public void gymCrmRecordsATraining(int duration, String username, String firstName, String lastName,
                                        String date, String actionType) {
        pendingEvent = new TrainingWorkloadEvent(
                username, firstName, lastName, true,
                LocalDate.parse(date), duration, WorkloadActionType.valueOf(actionType));
    }

    @When("the event is published to the training-workload queue")
    public void theEventIsPublishedToTheQueue() throws Exception {
        String json = objectMapper.writeValueAsString(pendingEvent);
        jmsTemplate.convertAndSend(queueName, json);
    }

    @When("an event with a missing trainer username is published to the training-workload queue")
    public void aMalformedEventIsPublished() {
        String malformedJson = "{\"trainerUsername\":null,\"trainerFirstName\":\"X\","
                + "\"trainerLastName\":\"Y\",\"isActive\":true,\"trainingDate\":\"2026-08-01\","
                + "\"trainingDuration\":10,\"actionType\":\"ADD\"}";
        jmsTemplate.convertAndSend(queueName, malformedJson);
    }

    @Then("workload-service should eventually report {int} minutes for trainer {string} for year {int} month {int}")
    @When("workload-service eventually reports {int} minutes for trainer {string} for year {int} month {int}")
    public void workloadServiceEventuallyReports(int minutes, String username, int year, int month) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/workloads/{username}/monthly", username)
                                .param("year", String.valueOf(year))
                                .param("month", String.valueOf(month)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.trainingSummaryDuration").value(minutes)));
    }

    @Then("the trainer {string} workload should still show {int} minutes for year {int} month {int}")
    public void theTrainerWorkloadShouldStillShow(String username, int minutes, int year, int month) throws Exception {
        // Give the (rejected) malformed message a moment to be handled/discarded before asserting
        // that the previously stored data was left untouched.
        Thread.sleep(1000);
        mockMvc.perform(get("/api/v1/workloads/{username}/monthly", username)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingSummaryDuration", equalTo(minutes)));
    }
}
