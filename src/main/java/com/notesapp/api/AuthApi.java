package com.notesapp.api;

import com.notesapp.config.ConfigReader;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

/**
 * AuthApi
 *
 * Handles POST /users/login and DELETE /users/logout.
 * loginAndGetToken() is the most-called method in the framework —
 * every API and E2E test calls it in @BeforeClass or @BeforeMethod.
 */
public class AuthApi {

    private static final Logger log = LoggerFactory.getLogger(AuthApi.class);

    /** Login with credentials from config.properties and return the token. */
    public static String loginAndGetToken() {
        return loginAndGetToken(ConfigReader.get("test.email"), ConfigReader.get("test.password"));
    }

    /** Login with specific credentials and return the token. */
    public static String loginAndGetToken(String email, String password) {
        log.info("API login — email: {}", email);

        String body = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);

        Response response = given()
                .spec(ApiConfig.getBaseSpec())
                .body(body)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String token = response.jsonPath().getString("data.token");
        log.info("Token obtained — prefix: {}...", token.substring(0, Math.min(6, token.length())));
        return token;
    }

    /**
     * Login and return the full Response without asserting status.
     * Used in negative tests where we expect a non-200 back.
     */
    public static Response loginRaw(String email, String password) {
        log.info("Raw login attempt — email: {}", email);
        String body = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        return given().spec(ApiConfig.getBaseSpec()).body(body).when().post("/users/login");
    }

    /** Invalidates the session on the server side. */
    public static Response logout(String token) {
        log.info("Logging out via API");
        return given().spec(ApiConfig.getAuthSpec(token)).when().delete("/users/logout");
    }
}
