Feature: Notes REST API Services

  Background:
    Given I authenticate with API and obtain token

  @api @get_notes @TC-API-01 @smoke
  Scenario: GET /notes - Returns List with Correct Structure
    When I get all notes via API
    Then the response status code should be 200
    And the response time should be less than 2000 milliseconds
    And the response should contain notes with fields id, title, category, description

  @api @create_note @TC-API-02
  Scenario: POST /notes - Create Note via API
    When I create a note via API with category "Work", title "API Note", and description "This is created via API"
    Then the response status code should be 200
    And the note details should be correct in response
    When I retrieve the created note by ID via API
    Then the response status code should be 200
    And the retrieved note title should be "API Note"

  @api @delete_note @TC-API-03
  Scenario: DELETE /notes/:id - Remove Note via API
    When I create a note via API with category "Work", title "Note To Delete via API", and description "Will be deleted"
    Then the response status code should be 200
    And the note details should be correct in response
    When I delete the created note via API
    Then the response status code should be 200
    And the deleted note should no longer be retrievable via API

  @api @negative @TC-API-04
  Scenario: GET /notes with Invalid Auth Token - Expect 401
    When I get all notes via API using an invalid token
    Then the response status code should be 401

  @api @negative @TC-API-05
  Scenario: DELETE /notes/:id with Non-Existent ID - Expect 404
    When I delete a note with a non-existent ID via API
    Then the response status code should be 404

  @api @negative @TC-NEG-02
  Scenario: POST /notes with Empty Title - Expect 400
    When I create a note via API with an empty title
    Then the response status code should be 400

  @api @negative
  Scenario Outline: API validation checks for invalid requests
    When I attempt to call API endpoint with "<scenario_type>"
    Then the API response status code should be <expected_status>

    Examples:
      | scenario_type          | expected_status |
      | invalid credentials    | 401             |
      | unauthorized token     | 401             |
