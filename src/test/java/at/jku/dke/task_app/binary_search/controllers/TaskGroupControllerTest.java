package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.binary_search.ClientSetupExtension;
import at.jku.dke.task_app.binary_search.DatabaseSetupExtension;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskGroupRepository;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class TaskGroupControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BinarySearchTaskGroupRepository repository;

    private long taskGroupId;

    @BeforeEach
    void initDb() {
        this.repository.deleteAll();
        this.taskGroupId = this.repository.save(new BinarySearchTaskGroup(1L, TaskStatus.APPROVED, 1, 5)).getId();
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
            .get("/api/taskGroup/{id}", this.taskGroupId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("minNumber", equalTo(1))
            .body("maxNumber", equalTo(5));
    }

    @Test
    void getShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/taskGroup/{id}", this.taskGroupId + 1)
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
            .get("/api/taskGroup/{id}", this.taskGroupId)
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
            .body(new ModifyTaskGroupDto<>("binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 5)))
            // WHEN
            .when()
            .post("/api/taskGroup/{id}", this.taskGroupId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Location", containsString("/api/taskGroup/" + (this.taskGroupId + 2)))
            .body("descriptionDe", containsString("5"))
            .body("descriptionEn", containsString("5"));
    }

    @Test
    void createShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskGroupDto<>("", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 5)))
            // WHEN
            .when()
            .post("/api/taskGroup/{id}", this.taskGroupId + 2)
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
            .post("/api/taskGroup/{id}", this.taskGroupId + 2)
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
            .body(new ModifyTaskGroupDto<>("binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 5)))
            // WHEN
            .when()
            .post("/api/taskGroup/{id}", this.taskGroupId + 2)
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
            .body(new ModifyTaskGroupDto<>("binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 10)))
            // WHEN
            .when()
            .put("/api/taskGroup/{id}", this.taskGroupId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("descriptionDe", containsString("10"))
            .body("descriptionEn", containsString("10"));
    }

    @Test
    void updateShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskGroupDto<>("binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 10)))
            // WHEN
            .when()
            .put("/api/taskGroup/{id}", this.taskGroupId + 1)
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
            .body(new ModifyTaskGroupDto<>("sql", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 10)))
            // WHEN
            .when()
            .put("/api/taskGroup/{id}", this.taskGroupId)
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
            .put("/api/taskGroup/{id}", this.taskGroupId)
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
            .body(new ModifyTaskGroupDto<>("binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 10)))
            // WHEN
            .when()
            .put("/api/taskGroup/{id}", this.taskGroupId)
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
            .delete("/api/taskGroup/{id}", this.taskGroupId)
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
            .delete("/api/taskGroup/{id}", this.taskGroupId + 1)
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
            .delete("/api/taskGroup/{id}", this.taskGroupId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- RANDOM NUMBER ---
    @Test
    void getRandomNumbers() {
        // Arrange
        var controller = new TaskGroupController(null);

        // Act
        var result = controller.getRandomNumbers();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertTrue(result.getBody().min() < 100);
        assertTrue(result.getBody().max() < 1000);
        assertTrue(result.getBody().min() >= 0);
        assertTrue(result.getBody().min() < result.getBody().max());
    }

    @Test
    void getRandomNumbersShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/taskGroup/random")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("min", Matchers.isA(Integer.class))
            .body("max", Matchers.isA(Integer.class));
    }

    @Test
    void getRandomNumbersShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/taskGroup/random")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    @Test
    void mapToDto() {
        // Arrange
        var taskGroup = new BinarySearchTaskGroup(1, 5);

        // Act
        var result = new TaskGroupController(null).mapToDto(taskGroup);

        // Assert
        assertEquals(1, result.minNumber());
        assertEquals(5, result.maxNumber());
    }
}
