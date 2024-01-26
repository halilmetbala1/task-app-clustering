package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.binary_search.ClientSetupExtension;
import at.jku.dke.task_app.binary_search.DatabaseSetupExtension;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskGroupRepository;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskRepository;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class TaskControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BinarySearchTaskRepository repository;

    @Autowired
    private BinarySearchTaskGroupRepository groupRepository;

    private long taskId;
    private long taskGroupId;

    @BeforeEach
    void initDb() {
        this.repository.deleteAll();

        var group = this.groupRepository.save(new BinarySearchTaskGroup(1L, TaskStatus.APPROVED, 1, 10));
        this.taskGroupId = group.getId();
        this.taskId = this.repository.save(new BinarySearchTask(1L, BigDecimal.TWO, TaskStatus.APPROVED, group, 5)).getId();
    }

    //#region --- GET ---
    @Test
    void getShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("solution", equalTo(5));
    }

    @Test
    void getShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void getShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- CREATE ---
    @Test
    void createShouldReturnCreated() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(6)))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Location", containsString("/api/task/" + (this.taskId + 2)))
            .body("descriptionDe", any(String.class))
            .body("descriptionEn", any(String.class));
    }

    @Test
    void createShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(6)))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void createShouldReturnBadRequestOnEmptyBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void createShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(6)))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- UPDATE ---
    @Test
    void updateShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(9)))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("descriptionDe", any(String.class))
            .body("descriptionEn", any(String.class));
    }

    @Test
    void updateShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(9)))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void updateShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(9)))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void updateShouldReturnBadRequestOnEmptyBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void updateShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(9)))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- DELETE ---
    @Test
    void deleteShouldReturnNoContent() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturnNoContentOnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    @Test
    void mapToDto() {
        // Arrange
        var task = new BinarySearchTask(42);

        // Act
        var result = new TaskController(null).mapToDto(task);

        // Assert
        assertEquals(42, result.solution());
    }

}
