@integration
Feature: Training workload integration between microservices
  As the platform
  I want workload-service to correctly consume the training events published by gym-crm
  So that a trainer's monthly workload always reflects the trainings recorded in the main service

  Scenario: A training-added event published by gym-crm updates the trainer's workload
    Given gym-crm records a training of 45 minutes for trainer "trainer.integration" "Ivy" "Integration" on "2026-06-15" with action "ADD"
    When the event is published to the training-workload queue
    Then workload-service should eventually report 45 minutes for trainer "trainer.integration" for year 2026 month 6

  Scenario: A training-removed event published by gym-crm decreases the trainer's workload
    Given gym-crm records a training of 45 minutes for trainer "trainer.integration2" "Ida" "Integration" on "2026-07-01" with action "ADD"
    And the event is published to the training-workload queue
    And workload-service eventually reports 45 minutes for trainer "trainer.integration2" for year 2026 month 7
    When gym-crm records a training of 15 minutes for trainer "trainer.integration2" "Ida" "Integration" on "2026-07-01" with action "DELETE"
    And the event is published to the training-workload queue
    Then workload-service should eventually report 30 minutes for trainer "trainer.integration2" for year 2026 month 7

  Scenario: A malformed event on the queue is rejected without crashing the consumer or corrupting other data
    Given gym-crm records a training of 20 minutes for trainer "trainer.integration3" "Ike" "Integration" on "2026-08-01" with action "ADD"
    And the event is published to the training-workload queue
    And workload-service eventually reports 20 minutes for trainer "trainer.integration3" for year 2026 month 8
    When an event with a missing trainer username is published to the training-workload queue
    Then the trainer "trainer.integration3" workload should still show 20 minutes for year 2026 month 8
