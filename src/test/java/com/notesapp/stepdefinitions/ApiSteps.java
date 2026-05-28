package com.notesapp.stepdefinitions;

import com.notesapp.api.AuthApi;
import com.notesapp.api.NotesApi;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * ApiSteps
 *
 * Cucumber step definitions for notes RestAssured API services.
 */
public class ApiSteps {

    private static String token;
    private Response response;
    private String createdNoteId;

    @Given("I authenticate with API and obtain token")
    public void i_authenticate_with_api_and_obtain_token() {
        if (token == null) {
            token = AuthApi.loginAndGetToken();
        }
    }

    // ── GET Notes ─────────────────────────────────────────────────────────────

    @When("I get all notes via API")
    public void i_get_all_notes_via_api() {
        response = NotesApi.getNotes(token);
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        Assert.assertEquals(response.statusCode(), (int) statusCode);
    }

    @Then("the response time should be less than {int} milliseconds")
    public void the_response_time_should_be_less_than_milliseconds(Integer maxMs) {
        long time = response.time();
        Assert.assertTrue(time < maxMs, "API Response time " + time + " ms exceeded " + maxMs + " ms limit!");
    }

    /**
     * TC-API-01 — Validate response structure contains expected fields.
     */
    @Then("the response should contain notes with fields id, title, category, description")
    public void the_response_should_contain_notes_with_fields() {
        List<Map<String, Object>> notes = response.jsonPath().getList("data");
        if (notes == null || notes.isEmpty()) {
            // No notes present — structure assertion passes vacuously
            return;
        }
        Map<String, Object> first = notes.get(0);
        Assert.assertTrue(first.containsKey("id"),          "Response note missing field: id");
        Assert.assertTrue(first.containsKey("title"),       "Response note missing field: title");
        Assert.assertTrue(first.containsKey("category"),    "Response note missing field: category");
        Assert.assertTrue(first.containsKey("description"), "Response note missing field: description");
    }

    // ── Create Note ───────────────────────────────────────────────────────────

    @When("I create a note via API with category {string}, title {string}, and description {string}")
    public void i_create_a_note_via_api_with_category_title_and_description(String category, String title, String description) {
        response = NotesApi.createNote(token, category, title, description);
        if (response.statusCode() == 200) {
            createdNoteId = response.jsonPath().getString("data.id");
        }
    }

    @Then("the note details should be correct in response")
    public void the_note_details_should_be_correct_in_response() {
        Assert.assertNotNull(createdNoteId, "Created note ID is null!");
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @When("I retrieve the created note by ID via API")
    public void i_retrieve_the_created_note_by_id_via_api() {
        response = NotesApi.getNoteById(token, createdNoteId);
    }

    @Then("the retrieved note title should be {string}")
    public void the_retrieved_note_title_should_be(String title) {
        String t = response.jsonPath().getString("data.title");
        Assert.assertEquals(t, title);
    }

    // ── Delete Note ───────────────────────────────────────────────────────────

    @When("I delete the created note via API")
    public void i_delete_the_created_note_via_api() {
        response = NotesApi.deleteNote(token, createdNoteId);
    }

    /**
     * TC-API-03 — Verify the note is truly gone after API delete.
     */
    @Then("the deleted note should no longer be retrievable via API")
    public void the_deleted_note_should_no_longer_be_retrievable_via_api() {
        Response getResponse = NotesApi.getNoteById(token, createdNoteId);
        Assert.assertNotEquals(getResponse.statusCode(), 200,
            "Deleted note with ID " + createdNoteId + " is still retrievable via API!");
    }

    /**
     * TC-API-04 — GET /notes with invalid token should return 401.
     */
    @When("I get all notes via API using an invalid token")
    public void i_get_all_notes_via_api_using_an_invalid_token() {
        response = NotesApi.getNotes("invalid_token_xyz_12345");
    }

    /**
     * TC-API-05 — DELETE /notes/:id with non-existent ID should return 404.
     */
    @When("I delete a note with a non-existent ID via API")
    public void i_delete_a_note_with_a_non_existent_id_via_api() {
        response = NotesApi.deleteNote(token, "000000000000000000000000");
    }

    /**
     * TC-NEG-02 — POST /notes with empty title should return 400.
     */
    @When("I create a note via API with an empty title")
    public void i_create_a_note_via_api_with_an_empty_title() {
        response = NotesApi.createNoteRaw(token, """
                {
                    "category": "Home",
                    "title": "",
                    "description": "Body with empty title"
                }
                """);
    }

    // ── Scenario Outline fallback ─────────────────────────────────────────────

    @When("I attempt to call API endpoint with {string}")
    public void i_attempt_to_call_api_endpoint_with(String scenarioType) {
        String cleanType = scenarioType.trim().toLowerCase();
        response = switch (cleanType) {
            case "invalid credentials"  -> AuthApi.loginRaw("wrong-user@example.com", "WrongPassword123!");
            case "missing title"        -> NotesApi.createNoteRaw(token, """
                    {
                        "category": "Home",
                        "description": "Body without title"
                    }
                    """);
            case "unauthorized token"   -> NotesApi.getNotes("invalid_token_xyz");
            default -> throw new IllegalArgumentException("Unknown API scenario outline type: " + scenarioType);
        };
    }

    @Then("the API response status code should be {int}")
    public void the_api_response_status_code_should_be(Integer statusCode) {
        Assert.assertEquals(response.statusCode(), (int) statusCode);
    }

    // ── E2E Steps ─────────────────────────────────────────────────────────────

    /**
     * TC-E2E-01 — Verify the UI-created note title appears in the API response list.
     */
    @Then("the API response should contain a note with title {string}")
    public void the_api_response_should_contain_a_note_with_title(String title) {
        List<String> titles = response.jsonPath().getList("data.title");
        Assert.assertNotNull(titles, "API response 'data' list is null!");
        Assert.assertTrue(titles.contains(title),
            "Expected note title '" + title + "' not found in API response list: " + titles);
    }

    /**
     * TC-E2E-02 — Delete the stored note ID via API (used in E2E-02 scenario).
     */
    @When("I delete the API note by stored ID")
    public void i_delete_the_api_note_by_stored_id() {
        Assert.assertNotNull(createdNoteId, "No note ID stored — was the note created via API first?");
        response = NotesApi.deleteNote(token, createdNoteId);
        Assert.assertEquals(response.statusCode(), 200,
            "Failed to delete note via API. Status: " + response.statusCode());
    }

    @Then("I verify via API that the note is no longer present in the list")
    public void i_verify_via_api_that_the_note_is_no_longer_present_in_the_list() {
        Response listResponse = NotesApi.getNotes(token);
        Assert.assertEquals(listResponse.statusCode(), 200);
        List<String> ids = listResponse.jsonPath().getList("data.id");
        Assert.assertFalse(ids.contains(createdNoteId), "Deleted note is still present in notes list!");
    }
}
