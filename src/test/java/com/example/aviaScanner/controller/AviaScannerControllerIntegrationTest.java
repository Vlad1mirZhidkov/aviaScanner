package com.example.aviaScanner.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.jupiter.api.BeforeAll;
import java.time.LocalDate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import com.example.aviaScanner.DTO.AviaScanerUserDTO;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.equalTo;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AviaScannerControllerIntegrationTest {
    
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
        .withDatabaseName("avia_test")
        .withUsername("postgres")
        .withPassword("1234");

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void beforeAll(){
        postgres.start();
    }

    @AfterAll
    static void afterAll(){
        postgres.stop();
    }
    

    @Test
    @Validate
    void cuccessTestCreateUser() {
        AviaScanerUserDTO AviaScanerUserDTO = new AviaScanerUserDTO();
        AviaScanerUserDTO.setName("Test_User");
        AviaScanerUserDTO.setEmail("test1@example.com");
        AviaScanerUserDTO.setPhone("+79609062424");
        AviaScanerUserDTO.setLocation("Test_Location");
        AviaScanerUserDTO.setBirthDate(LocalDate.of(1990, 1, 1));

        System.out.println("Request body: " + AviaScanerUserDTO);

        AviaScanerUserDTO response = given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(AviaScanerUserDTO)
        .when()
            .post("/api/users")
        .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(ContentType.JSON)
            .body("name", equalTo("Test_User"))
            .body("email", equalTo("test1@example.com"))
            .body("location", equalTo("Test_Location"))
            .body("phone", equalTo("+79609062424"))
            .extract()
            .as(AviaScanerUserDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void deniedTestCreateUser() {
        AviaScanerUserDTO userDTO = new AviaScanerUserDTO();
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

}
