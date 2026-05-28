package com.notesapp.stepdefinitions;

import com.notesapp.pages.NotesPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Given;
import org.testng.Assert;

/**
 * NoteSteps
 *
 * Cucumber step definitions for notes dashboard UI scenarios.
 */
public class NoteSteps {

    private final NotesPage notesPage = new NotesPage();

    @Given("I create a note with category {string}, title {string}, and description {string}")
    public void i_create_a_note_with_category_title_and_description(String category, String title, String description) {
        notesPage.addNote(category, title, description);
    }

    @Then("the note with title {string} should be present on the dashboard")
    public void the_note_with_title_should_be_present_on_the_dashboard(String title) {
        Assert.assertTrue(notesPage.isNotePresent(title), "Note with title '" + title + "' was not found on the dashboard!");
    }

    @When("I delete the note with title {string}")
    public void i_delete_the_note_with_title(String title) {
        boolean deleted = notesPage.deleteNoteByTitle(title);
        Assert.assertTrue(deleted, "Failed to locate and click delete on the note: " + title);
    }

    @Then("the note with title {string} should not be present on the dashboard")
    public void the_note_with_title_should_not_be_present_on_the_dashboard(String title) {
        Assert.assertTrue(notesPage.waitForNoteToDisappear(title), "Note with title '" + title + "' is still visible on the dashboard!");
    }

    @When("I submit a blank note")
    public void i_submit_a_blank_note() {
        notesPage.submitBlankNote();
    }

    @Then("I should see validation errors for required fields")
    public void i_should_see_validation_errors_for_required_fields() {
        Assert.assertTrue(notesPage.isValidationErrorVisible(), "Validation errors were not displayed!");
        String errorText = notesPage.getValidationErrorText();
        logErrorText(errorText);
    }

    /**
     * TC-UI-03 — Edit an Existing Note via UI
     * Opens the note's edit form, updates title and description, and saves.
     */
    @When("I edit the note with title {string} to new title {string} and description {string}")
    public void i_edit_the_note_with_title_to_new_title_and_description(String oldTitle, String newTitle, String newDescription) {
        boolean edited = notesPage.editNoteByTitle(oldTitle, newTitle, newDescription);
        Assert.assertTrue(edited, "Failed to locate and edit the note with title: " + oldTitle);
    }

    private void logErrorText(String text) {
        System.out.println("Form validation feedback: " + text);
    }
}
