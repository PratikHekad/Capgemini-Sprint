Feature: End-to-End UI and API Synchronization

  @e2e @sync @TC-E2E-01
  Scenario: UI-Created Note Appears in API Response - Field Match
    Given I authenticate with API and obtain token
    And I navigate to the login page
    And I login with valid credentials
    When I create a note with category "Work", title "UI Created E2E Note", and description "Created via UI for E2E"
    Then the note with title "UI Created E2E Note" should be present on the dashboard
    When I get all notes via API
    Then the response status code should be 200
    And the API response should contain a note with title "UI Created E2E Note"
    When I delete the note with title "UI Created E2E Note"
    Then the note with title "UI Created E2E Note" should not be present on the dashboard

  @e2e @sync @TC-E2E-02
  Scenario: API Delete Reflects in UI - Note Disappears
    Given I authenticate with API and obtain token
    When I create a note via API with category "Home", title "API Created For UI Delete", and description "To be deleted via API"
    Then the response status code should be 200
    And the note details should be correct in response
    And I navigate to the login page
    And I login with valid credentials
    Then the note with title "API Created For UI Delete" should be present on the dashboard
    When I delete the API note by stored ID
    And I refresh the page
    Then the note with title "API Created For UI Delete" should not be present on the dashboard

  @e2e @sync @TC-E2E-03 @smoke
  Scenario: POST /notes via API - Note Visible in UI
    Given I authenticate with API and obtain token
    When I create a note via API with category "Home", title "E2E Sync Note", and description "Sync test"
    And I navigate to the login page
    And I login with valid credentials
    Then the note with title "E2E Sync Note" should be present on the dashboard
    When I delete the note with title "E2E Sync Note"
    Then the note with title "E2E Sync Note" should not be present on the dashboard
    And I verify via API that the note is no longer present in the list
