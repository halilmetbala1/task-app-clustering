package at.jku.dke.task_app.clustering.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.ClientSetupExtension;
import at.jku.dke.task_app.clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusterRepository;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import at.jku.dke.task_app.clustering.dto.ClusteringTaskDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class TaskControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ClusteringTaskRepository repository;

    @Autowired
    private ClusterRepository clusterRepository;

    private long taskId;
    private long taskGroupId;

    @BeforeEach
    void initDb() {
        this.repository.deleteAll();

        ClusteringTask task = new ClusteringTask(2L,
            BigDecimal.TWO,
            TaskStatus.APPROVED,
            3,
            DistanceMetric.EUCLIDEAN,
            DifficultyLevel.EASY
        );
        //ClusteringTaskGenerator.generateAndSolve(task);

        this.taskId = this.repository.save(task).getId();
    }

    @Test
    void getShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            .when()
            .get("/api/task/{id}", this.taskId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("numberOfClusters", equalTo(3));
    }

    @Test
    void getShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            .when()
            .get("/api/task/{id}", this.taskId + 1)
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
            .when()
            .get("/api/task/{id}", this.taskId)
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }

    @Test
    void createShouldReturnCreated() {
        ModifyClusteringTaskDto newDto = new ModifyClusteringTaskDto(
            3,
            DistanceMetric.EUCLIDEAN,
            DifficultyLevel.MEDIUM
        );

        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "clustering", TaskStatus.APPROVED, newDto))
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Location", containsString("/api/task/" + (this.taskId + 2)))
            .body("numberOfClusters", equalTo(3));
    }

    @Test
    void updateShouldReturnOk() {
        ModifyClusteringTaskDto updateDto = new ModifyClusteringTaskDto(
            4,
            DistanceMetric.MANHATTAN,
            DifficultyLevel.HARD
        );

        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "clustering", TaskStatus.APPROVED, updateDto))
            .when()
            .put("/api/task/{id}", this.taskId)
            .then()
            .log().ifValidationFails()
            .statusCode(200);
    }

    @Test
    void deleteShouldReturnNoContent() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .when()
            .delete("/api/task/{id}", this.taskId)
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }
/*
    @Test
    void mapToDtoShouldReturnExpectedValues() {
        ClusteringTask task = new ClusteringTask(
            BigDecimal.ONE,
            TaskStatus.APPROVED,
            new ClusteringTaskGroup(2L, TaskStatus.APPROVED, 1, 10),
            2,
            20,
            DistanceMetric.MANHATTAN,
            DifficultyLevel.EASY
        );
        ClusteringTaskGenerator.generateAndSolve(task);

        ClusteringTaskDto dto = ClusteringTaskDto.fromTask(task);

        assertEquals(2, dto.numberOfClusters());
        assertEquals("MANHATTAN", dto.distanceMetric());
        assertFalse(dto.clusters().isEmpty());
    }*/
}
