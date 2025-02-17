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
        postgres.start();
    }

    @AfterAll
    static void afterAll(){
        postgres.stop();
    }

    private void assertErrorResponseNotFoundFields(ErrorResponse errorResponse, int status, String path) {
        assertEquals(status, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("User not found", errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    private void assertErrorResponseBadRequestFields(ErrorResponse errorResponse) {
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("Validation failed"));
        assertEquals("/api/users", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    private AviaScannerUserDTO createExistingUser(){
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

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void whenCreateUserWithInvalidData_thenReturnBadRequest() {
        AviaScannerUserDTO userDTO = new AviaScannerUserDTO();
        userDTO.setName("Volodya");
        userDTO.setLocation("Moscow never sleep");

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

        assertErrorResponseBadRequestFields(errorResponse);
    }

    @Test 
    void whenGetExistingUser_thenReturnUserData(){
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

        assertNotNull(actualUser.getName());
        assertNotNull(actualUser.getEmail());
        assertNotNull(actualUser.getLocation());
        assertNotNull(actualUser.getPhone());
        assertNotNull(actualUser.getBirthDate());
    }

    @Test 
    void whenGetNonExistingUser_thenReturnNotFound(){
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

        assertErrorResponseNotFoundFields(errorResponse, 404, "/api/users/100");
    }

    @Test
    void whenDeleteExistingUser_thenUserDeleted(){
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/users/6")
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    void whenDeleteNonExistingUser_thenReturnNotFound(){
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

        assertErrorResponseNotFoundFields(errorResponse, 404, "/api/users/100");
    }

    @Test
    void whenUpdateExistingUserWithValidData_thenUserUpdated() {
        AviaScannerUserDTO expectedUser = new AviaScannerUserDTO();
        expectedUser.setName("Updated_User");
        expectedUser.setEmail("updated@example.com");
        expectedUser.setPhone("+79609062425");
        expectedUser.setLocation("Updated_Location");

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

        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getPhone(), actualUser.getPhone());
        assertEquals(expectedUser.getLocation(), actualUser.getLocation());
    }

    @Test
    void whenUpdateNonExistingUser_thenReturnNotFound(){
        AviaScannerUserDTO updates = new AviaScannerUserDTO();
        updates.setName("Updated_User");
        updates.setEmail("updated@example.com");
        updates.setPhone("+79609062425");
        updates.setLocation("Updated_Location");

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

        assertErrorResponseNotFoundFields(errorResponse, 404, "/api/users/999");
    }
}
