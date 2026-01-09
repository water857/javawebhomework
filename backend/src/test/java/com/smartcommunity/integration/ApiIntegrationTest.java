package com.smartcommunity.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiIntegrationTest {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080/backend";
    private static final String USERNAME = "zhangsan";
    private static final String PASSWORD = "password";
    private static final int MESSAGE_TARGET_USER_ID = 2;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = System.getProperty("baseUrl", DEFAULT_BASE_URL);
    }

    @Test
    void loginSuccessReturnsToken() {
        String token = loginAndGetToken();
        assertNotNull(token);
        assertTrue(token.length() > 10);
    }

    @Test
    void loginFailsWithWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", USERNAME, "password", "wrong-password"))
            .when()
            .post("/api/login")
            .then()
            .statusCode(401);
    }

    @Test
    void loginFailsWithMissingFields() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", USERNAME))
            .when()
            .post("/api/login")
            .then()
            .statusCode(400);
    }

    @Test
    void privateMessageFlowWorks() {
        String token = loginAndGetToken();

        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/message/list")
            .then()
            .statusCode(200);

        String content = "Integration test message " + System.currentTimeMillis();
        Response sendResponse = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(Map.of("toUserId", MESSAGE_TARGET_USER_ID, "content", content))
            .when()
            .post("/api/message/send")
            .then()
            .statusCode(200)
            .extract()
            .response();

        Integer messageId = sendResponse.jsonPath().getInt("data.messageId");
        assertNotNull(messageId);

        given()
            .header("Authorization", "Bearer " + token)
            .queryParam("withUserId", MESSAGE_TARGET_USER_ID)
            .when()
            .get("/api/message/list")
            .then()
            .statusCode(200);

        Response readResponse = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(Map.of("messageId", messageId))
            .when()
            .post("/api/message/read")
            .then()
            .statusCode(200)
            .extract()
            .response();

        Integer updated = readResponse.jsonPath().getInt("data.updated");
        assertNotNull(updated);
    }

    private String loginAndGetToken() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", USERNAME, "password", PASSWORD))
            .when()
            .post("/api/login")
            .then()
            .statusCode(200)
            .extract()
            .response();

        JsonPath jsonPath = response.jsonPath();
        String token = jsonPath.getString("data.token");
        assertNotNull(token);
        return token;
    }
}
