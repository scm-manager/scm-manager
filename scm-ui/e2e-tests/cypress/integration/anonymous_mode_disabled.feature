Feature: Anonymous Mode Disabled

  Background:
    Given Anonymous Mode is disabled

  Scenario: There is no primary navigation
    Given User is not authenticated
    When User visits any page
    Then There is no primary navigation

  Scenario: Authenticated users have a footer navigation
    Given User is authenticated
    When User visits any page
    Then There is a footer navigation
