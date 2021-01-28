Feature: Anonymous Mode Full

  Background:
    Given Full Anonymous Mode is enabled
    And User is not authenticated

  Scenario: Show login button on anonymous route
    Given Anonymous user has permission to read all repositories
    When User visits the repository overview page
    Then The repository overview page is shown
    And There is a login button

  Scenario: Show login page
    When User visits login page
    Then The login page is shown

  Scenario: Navigate to login page
    When Users clicks login button
    Then The login page is shown

  Scenario: Redirect to repositories overview after login
    When User logs in
    Then User should be authenticated

  Scenario: Anonymous user cannot change password
    When User visits their user settings
    Then There is no option to change the password
