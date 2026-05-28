package com.notesapp.api;

import com.notesapp.config.ConfigReader;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApiConfig
 *
 * Single source of RequestSpecification objects.
 * Every API helper calls getBaseSpec() or getAuthSpec() — never builds its own.
 */
public class ApiConfig {

    private static final Logger log = LoggerFactory.getLogger(ApiConfig.class);

    private ApiConfig() {}

    /** No auth header — for login and register endpoints. */
    public static RequestSpecification getBaseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())  // auto-attaches to Allure report
                .log(LogDetail.ALL)
                .build();
    }

    /** Includes X-Auth-Token — for all protected endpoints. */
    public static RequestSpecification getAuthSpec(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(getBaseSpec())
                .addHeader("X-Auth-Token", token)
                .build();
    }

    /** Call once in @BeforeSuite. */
    public static void init() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        log.info("RestAssured initialized. Base URL: {}", ConfigReader.getApiBaseUrl());
    }
}
