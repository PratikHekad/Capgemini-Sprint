package com.notesapp.api;

import com.notesapp.api.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NotesApi
 *
 * Wraps all /notes endpoint calls:
 *   GET    /notes         — list notes
 *   POST   /notes         — create note
 *   DELETE /notes/{id}    — delete note
 */
public class NotesApi {
    private static final Logger log = LoggerFactory.getLogger(NotesApi.class);

    public static Response getNotes(String token) {
        log.info("API - Get all notes");
        return RestAssured.given()
                .spec(ApiConfig.getAuthSpec(token))
                .when()
                .get("/notes");
    }

    public static Response getNoteById(String token, String noteId) {
        log.info("API - Get note by ID: {}", noteId);
        return RestAssured.given()
                .spec(ApiConfig.getAuthSpec(token))
                .when()
                .get("/notes/" + noteId);
    }

    public static Response createNote(String token, String category, String title, String description) {
        log.info("API - Create note - Category: {}, Title: '{}'", category, title);
        JSONObject body = new JSONObject();
        body.put("category", category);
        body.put("title", title);
        body.put("description", description);
        return RestAssured.given()
                .spec(ApiConfig.getAuthSpec(token))
                .body(body.toString())
                .when()
                .post("/notes");
    }

    public static Response createNoteRaw(String token, String body) {
        log.info("API - Create note raw");
        return RestAssured.given()
                .spec(ApiConfig.getAuthSpec(token))
                .body(body)
                .when()
                .post("/notes");
    }

    public static Response updateNote(String token, String noteId, String category, String title, String description, boolean completed) {
        log.info("API - Update note ID: {}", noteId);
        JSONObject body = new JSONObject();
        body.put("category", category);
        body.put("title", title);
        body.put("description", description);
        body.put("completed", completed);
        return RestAssured.given()
                .spec(ApiConfig.getAuthSpec(token))
                .body(body.toString())
                .when()
                .put("/notes/" + noteId);
    }

    public static Response deleteNote(String token, String noteId) {
        log.info("API - Delete note ID: {}", noteId);
        return RestAssured.given()
                .spec(ApiConfig.getAuthSpec(token))
                .when()
                .delete("/notes/" + noteId);
    }
}
