Feature: Notes Management UI

  Background:
    Given I navigate to the login page
    And I login with valid credentials

  @ui @create_note @TC-UI-02
  Scenario: Create a Note via UI with All Fields
    When I create a note with category "Home", title "Cucumber Note", and description "This is created via Cucumber UI scenario"
    Then the note with title "Cucumber Note" should be present on the dashboard

  @ui @edit_note @TC-UI-03
  Scenario: Edit an Existing Note via UI
    Given I create a note with category "Work", title "Note To Edit", and description "Original description"
    And the note with title "Note To Edit" should be present on the dashboard
    When I edit the note with title "Note To Edit" to new title "Edited Note Title" and description "Updated description"
    Then the note with title "Edited Note Title" should be present on the dashboard

  @ui @delete_note @TC-UI-04
  Scenario: Delete a Note via UI
    Given I create a note with category "Home", title "Note To Delete", and description "This note will be deleted"
    And the note with title "Note To Delete" should be present on the dashboard
    When I delete the note with title "Note To Delete"
    Then the note with title "Note To Delete" should not be present on the dashboard

  @ui @negative @TC-UI-05
  Scenario: Create Note with Missing Required Fields
    When I submit a blank note
    Then I should see validation errors for required fields
