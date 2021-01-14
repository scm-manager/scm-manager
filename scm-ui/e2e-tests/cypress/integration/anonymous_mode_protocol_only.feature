Feature: Anonymous Mode with Protocol Only
  Background:
    Given Protocol Only Anonymous Mode is enabled

  Scenario: There is no primary navigation
    Given User is not authenticated
    When User visits the repository overview page
    Then The login page is shown
