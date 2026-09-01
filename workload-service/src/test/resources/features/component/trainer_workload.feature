@component
Feature: Trainer workload management
  As the workload-service
  I want to record and report trainers' monthly training workload
  So that consumers can query how many training hours a trainer had in a given period

  Scenario: Adding a training creates a new workload record for a previously unknown trainer
    Given a workload update for trainer "trainer.new" "Nina" "New" active with 60 minutes of training on "2026-03-10" of type "ADD"
    When I submit the workload update request
    Then the workload update response status should be 200
    And the workload summary for trainer "trainer.new" should show 60 minutes for year 2026 month 3

  Scenario: Adding a second training accumulates the duration for the same month
    Given a workload update for trainer "trainer.acc" "Anna" "Acc" active with 60 minutes of training on "2026-04-05" of type "ADD"
    And I submit the workload update request
    When a workload update for trainer "trainer.acc" "Anna" "Acc" active with 30 minutes of training on "2026-04-20" of type "ADD" is submitted
    Then the workload update response status should be 200
    And the workload summary for trainer "trainer.acc" should show 90 minutes for year 2026 month 4

  Scenario: Deleting a training subtracts the duration
    Given a workload update for trainer "trainer.del" "Dan" "Del" active with 60 minutes of training on "2026-05-01" of type "ADD"
    And I submit the workload update request
    When a workload update for trainer "trainer.del" "Dan" "Del" active with 20 minutes of training on "2026-05-01" of type "DELETE" is submitted
    Then the workload update response status should be 200
    And the workload summary for trainer "trainer.del" should show 40 minutes for year 2026 month 5

  Scenario: Updating a workload with a missing trainer username is rejected
    Given a workload update for trainer "" "Nina" "New" active with 60 minutes of training on "2026-03-10" of type "ADD"
    When I submit the workload update request
    Then the workload update response status should be 400

  Scenario: Updating a workload with a non-positive duration is rejected
    Given a workload update for trainer "trainer.bad" "Bad" "Duration" active with -5 minutes of training on "2026-03-10" of type "ADD"
    When I submit the workload update request
    Then the workload update response status should be 400

  Scenario: Requesting the workload summary of an unknown trainer returns not found
    When I request the workload summary for trainer "no.such.trainer"
    Then the workload update response status should be 404
