Feature: Authentication
  As a registered user of the Gym CRM
  I want to log in with my credentials
  So that I can access protected resources

  Background:
    Given a trainee "Carol" "Danvers" is already registered

  @positive
  Scenario: Successful login with valid credentials
    When "Carol" "Danvers" logs in with the correct password
    Then the login response status should be 200
    And the response should contain a JWT access token

  @negative
  Scenario: Login fails with an incorrect password
    When "Carol" "Danvers" logs in with the password "totally-wrong-password"
    Then the login response status should be 401

  @negative
  Scenario: Login fails for a username that does not exist
    When user "no.such.user" logs in with the password "whatever123"
    Then the login response status should be 401

  @positive
  Scenario: A logged-in user can log out and the token is revoked
    Given "Carol" "Danvers" logs in with the correct password
    When "Carol" "Danvers" logs out
    Then the logout response status should be 204
