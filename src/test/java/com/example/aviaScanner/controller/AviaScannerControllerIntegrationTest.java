package com.example.aviaScanner.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.aviaScanner.DTO.AviaScannerUserDTO;
import com.example.aviaScanner.DTO.ErrorResponse;
import com.example.aviaScanner.model.AviaScanerUserEntity;
import com.example.aviaScanner.repository.AviaScanerUserRepository;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AviaScannerControllerIntegrationTest {
    private static Long createdUserId;
    
    @Autowired
    private AviaScanerUserRepository aviaScanerUserRepository;

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
        return AviaScannerUserDTO.builder()
            .name("Test_User")
            .email("test1@example.com")
            .phone("+79609062424")
            .location("Test_Location")
            .birthDate(LocalDate.of(1990, 1, 1))
            .build();
    }

    @Test
    @Order(1)
    void whenCreateUserWithValidData_thenUserCreatedSuccessfully() {
        log.info("Testing user creation with valid data");
        AviaScannerUserDTO expectedUser = createExistingUser();

        log.info("Database state before user creation:");
        logDatabaseState();

        log.info("Sending POST request to create user");
        AviaScannerUserDTO actualUser = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(expectedUser)
            .log().all()
        .when()
            .post("/api/users")
        .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(AviaScannerUserDTO.class);

        assertNotNull(actualUser.getId(), "Created user should have an ID");
        createdUserId = actualUser.getId();
        log.info("Created user with ID: {}", createdUserId);

        assertThat(actualUser)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(expectedUser);

        AviaScanerUserEntity savedUser = aviaScanerUserRepository.findById(createdUserId)
            .orElseThrow(() -> new AssertionError("User not found in database"));

        assertThat(savedUser)
            .satisfies(user -> {
                assertEquals(expectedUser.getName(), user.getName());
                assertEquals(expectedUser.getEmail(), user.getEmail()); 
                assertEquals(expectedUser.getPhone(), user.getPhone());
                assertEquals(expectedUser.getLocation(), user.getLocation());
                assertEquals(expectedUser.getBirthDate(), user.getBirthDate());
            });

        log.info("Database state after user creation:");
        logDatabaseState();
    }

    @Test
    void whenCreateUserWithInvalidData_thenReturnBadRequest() {
        log.info("Testing user creation with invalid data");
        AviaScannerUserDTO userDTO = AviaScannerUserDTO.builder()
            .name("Volodya")
            .location("Moscow never sleep")
            .build();
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
    @Order(2)
    void whenGetExistingUser_thenReturnUserData() {
        assertNotNull(createdUserId, "Created user ID should not be null");
        log.info("Testing get existing user with ID: {}", createdUserId);
        
        boolean exists = aviaScanerUserRepository.existsById(createdUserId);
        log.info("User exists in database before test: {}", exists);
        
        if (!exists) {
            log.warn("User not found in database. Current database content:");
            aviaScanerUserRepository.findAll().forEach(user -> 
                log.info("User in DB: {}", user));
        }

        log.info("Sending GET request for user");
        AviaScannerUserDTO actualUser = given()
            .port(port)
            .contentType(ContentType.JSON)
            .log().all()
        .when()
            .get("/api/users/" + createdUserId)
        .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .extract()
            .as(AviaScannerUserDTO.class);

        log.debug("Retrieved user: {}", actualUser);
        assertNotNull(actualUser.getId(), "User ID should not be null");
        assertEquals(createdUserId, actualUser.getId(), 
            "Retrieved user ID should match created user ID");
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
    @Order(3)
    void whenUpdateExistingUserWithValidData_thenUserUpdated() {
        log.info("Testing update existing user with valid data");
        AviaScannerUserDTO expectedUser = AviaScannerUserDTO.builder()
            .name("Updated_User")
            .email("updated@example.com")
            .phone("+79609062425")
            .location("Updated_Location")
            .build();
        log.debug("Update data: {}", expectedUser);

        AviaScannerUserDTO actualUser = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(expectedUser)
        .when()
            .patch("/api/users/" + createdUserId)
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
        AviaScannerUserDTO updates = AviaScannerUserDTO.builder()
            .name("Updated_User")
            .email("updated@example.com")
            .phone("+79609062425")
            .location("Updated_Location")
            .build();
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
    @Test
    @Order(4)
    void whenDeleteExistingUser_thenUserDeleted(){
        log.info("Testing delete existing user");
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/users/" + createdUserId)
        .then()
            .statusCode(HttpStatus.OK.value());
        log.debug("Successfully deleted user with id: {}", createdUserId);
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

    private void logDatabaseState() {
        List<AviaScanerUserEntity> allUsers = aviaScanerUserRepository.findAll();
        log.info("Total users in database: {}", allUsers.size());
        allUsers.forEach(user -> log.info("User: {}", user));
    }
}
