Feature: User registration
  As a visitor of the Gym CRM
  I want to register as a trainee or a trainer
  So that I can start using the platform

  @positive
  Scenario: Successful trainee registration
    Given I have trainee registration details with first name "Alice" and last name "Wonder"
    When I submit the trainee registration request
    Then the registration response status should be 200
    And the response should contain a generated username and password

  @negative
  Scenario: Trainee registration fails when the first name is missing
    Given I have trainee registration details with first name "" and last name "Wonder"
    When I submit the trainee registration request
    Then the registration response status should be 400

  @positive
  Scenario: Successful trainer registration
    Given I have trainer registration details with first name "Bob" and last name "Builder" and specialization id 1
    When I submit the trainer registration request
    Then the registration response status should be 200
    And the response should contain a generated username and password

  @negative
  Scenario: Trainer registration fails for an unknown specialization
    Given I have trainer registration details with first name "Bob" and last name "Builder" and specialization id 9999
    When I submit the trainer registration request
    Then the registration response status should be 400

  @negative
  Scenario: Trainer registration fails when the last name is missing
    Given I have trainer registration details with first name "Bob" and last name "" and specialization id 1
    When I submit the trainer registration request
    Then the registration response status should be 400
