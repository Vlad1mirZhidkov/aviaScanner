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
import java.util.Map;
import java.util.HashMap;


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
    

    @Test
    void whenCreateUserWithValidData_thenUserCreatedSuccessfully() {
        AviaScannerUserDTO expectedUser = new AviaScannerUserDTO();
        expectedUser.setName("Test_User");
        expectedUser.setEmail("test1@example.com");
        expectedUser.setPhone("+79609062424");
        expectedUser.setLocation("Test_Location");
        expectedUser.setBirthDate(LocalDate.of(1990, 1, 1));

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

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(userDTO)
        .when()
            .post("/api/users")
        .then()
            .log().body()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", org.hamcrest.Matchers.containsString("Validation failed"))
            .body("path", equalTo("/api/users"))
            .body("timestamp", org.hamcrest.Matchers.notNullValue());
    }

    @Test 
    void whenGetExistingUser_thenReturnUserData(){
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/users/6")
        .then()
            .log().body()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .body("name", org.hamcrest.Matchers.notNullValue())
            .body("email", org.hamcrest.Matchers.notNullValue())
            .body("location", org.hamcrest.Matchers.notNullValue())
            .body("phone", org.hamcrest.Matchers.notNullValue())
            .body("birthDate", org.hamcrest.Matchers.notNullValue());
    }

    @Test 
    void whenGetNonExistingUser_thenReturnNotFound(){
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/users/100")
        .then()
            .log().body()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(ContentType.JSON)
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"))
            .body("message", equalTo("User not found"))
            .body("path", equalTo("/api/users/100"));
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
        given()
            .port(port)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/users/100")
        .then()
            .log().body()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(ContentType.JSON)
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"))
            .body("message", equalTo("User not found"))
            .body("path", equalTo("/api/users/100"));
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
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated_User");
        updates.put("email", "updated@example.com");
        updates.put("phone", "+79609062425");
        updates.put("location", "Updated_Location");
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(updates)
        .when()
            .patch("/api/users/999")
        .then()
            .log().body()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(ContentType.JSON)
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"))
            .body("message", equalTo("User not found"))
            .body("path", equalTo("/api/users/999"));
    }
}
