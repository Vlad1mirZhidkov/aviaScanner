package com.example.aviaScanner.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.jupiter.api.BeforeAll;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import com.example.aviaScanner.DTO.AviaScannerUserDTO;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.equalTo;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.example.aviaScanner.DTO.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AviaScannerControllerIntegrationTest {
    
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
        .withDatabaseName("avia_test")
        .withUsername("postgres")
        .withPassword("1234");

    @LocalServerPort
    private Integer port;

    @BeforeAll
    static void beforeAll(){
        log.info("Starting PostgreSQL container");
        postgres.start();
    }

    @AfterAll
    static void afterAll(){
        log.info("Stopping PostgreSQL container");
        postgres.stop();
    }

    private void assertErrorResponseNotFoundFields(ErrorResponse errorResponse, int status, String path) {
        log.debug("Asserting Not Found error response fields. Status: {}, Path: {}", status, path);
        assertEquals(status, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("User not found", errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    private void assertErrorResponseBadRequestFields(ErrorResponse errorResponse) {
        log.debug("Asserting Bad Request error response fields for path: {}", errorResponse.getPath());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("Validation failed"));
        assertEquals("/api/users", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    private AviaScannerUserDTO createExistingUser(){
        log.debug("Creating test user DTO");
        AviaScannerUserDTO expectedUser = new AviaScannerUserDTO();
        expectedUser.setName("Test_User");
        expectedUser.setEmail("test1@example.com");
        expectedUser.setPhone("+79609062424");
        expectedUser.setLocation("Test_Location");
        expectedUser.setBirthDate(LocalDate.of(1990, 1, 1));
        return expectedUser;
    }

    @Test
    void whenCreateUserWithValidData_thenUserCreatedSuccessfully() {
        log.info("Testing user creation with valid data");
        AviaScannerUserDTO expectedUser = createExistingUser();

        AviaScannerUserDTO actualUser = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(expectedUser)
        .when()
            .post("/api/users")
        .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(AviaScannerUserDTO.class);

        log.debug("Created user: {}", actualUser);
        assertEquals(expectedUser, actualUser);
    }

    @Test
    void whenCreateUserWithInvalidData_thenReturnBadRequest() {
        log.info("Testing user creation with invalid data");
        AviaScannerUserDTO userDTO = new AviaScannerUserDTO();
        userDTO.setName("Volodya");
        userDTO.setLocation("Moscow never sleep");
        log.debug("Invalid user data: {}", userDTO);

        ErrorResponse errorResponse = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(userDTO)
        .when()
            .post("/api/users")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(ErrorResponse.class);

        log.debug("Received error response: {}", errorResponse);
        assertErrorResponseBadRequestFields(errorResponse);
    }

    @Test 
    void whenGetExistingUser_thenReturnUserData(){
        log.info("Testing get existing user");
        AviaScannerUserDTO actualUser = given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/users/6")
        .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(AviaScannerUserDTO.class);

        log.debug("Retrieved user: {}", actualUser);
        assertNotNull(actualUser.getName());
        assertNotNull(actualUser.getEmail());
        assertNotNull(actualUser.getLocation());
        assertNotNull(actualUser.getPhone());
        assertNotNull(actualUser.getBirthDate());
    }

    @Test 
    void whenGetNonExistingUser_thenReturnNotFound(){
        log.info("Testing get non-existing user");
        ErrorResponse errorResponse = given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/users/100")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(ErrorResponse.class);

        log.debug("Received error response: {}", errorResponse);
        assertErrorResponseNotFoundFields(errorResponse, 404, "/api/users/100");
    }

    @Test
    void whenDeleteExistingUser_thenUserDeleted(){
        log.info("Testing delete existing user");
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/users/6")
        .then()
            .statusCode(HttpStatus.OK.value());
        log.debug("Successfully deleted user with id: 6");
    }

    @Test
    void whenDeleteNonExistingUser_thenReturnNotFound(){
        log.info("Testing delete non-existing user");
        ErrorResponse errorResponse = given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/users/100")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(ErrorResponse.class);

        log.debug("Received error response: {}", errorResponse);
        assertErrorResponseNotFoundFields(errorResponse, 404, "/api/users/100");
    }

    @Test
    void whenUpdateExistingUserWithValidData_thenUserUpdated() {
        log.info("Testing update existing user with valid data");
        AviaScannerUserDTO expectedUser = new AviaScannerUserDTO();
        expectedUser.setName("Updated_User");
        expectedUser.setEmail("updated@example.com");
        expectedUser.setPhone("+79609062425");
        expectedUser.setLocation("Updated_Location");
        log.debug("Update data: {}", expectedUser);

        AviaScannerUserDTO actualUser = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(expectedUser)
        .when()
            .patch("/api/users/6")
        .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(AviaScannerUserDTO.class);

        log.debug("Updated user: {}", actualUser);
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getPhone(), actualUser.getPhone());
        assertEquals(expectedUser.getLocation(), actualUser.getLocation());
    }

    @Test
    void whenUpdateNonExistingUser_thenReturnNotFound(){
        log.info("Testing update non-existing user");
        AviaScannerUserDTO updates = new AviaScannerUserDTO();
        updates.setName("Updated_User");
        updates.setEmail("updated@example.com");
        updates.setPhone("+79609062425");
        updates.setLocation("Updated_Location");
        log.debug("Update data: {}", updates);

        ErrorResponse errorResponse = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(updates)
        .when()
            .patch("/api/users/999")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(ErrorResponse.class);

        log.debug("Received error response: {}", errorResponse);
        assertErrorResponseNotFoundFields(errorResponse, 404, "/api/users/999");
    }
}
