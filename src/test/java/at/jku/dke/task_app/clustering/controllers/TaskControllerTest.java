package at.jku.dke.task_app.clustering.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.ClientSetupExtension;
import at.jku.dke.task_app.clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.DataPoint;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusterRepository;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class TaskControllerTest {

    private MessageSource createTestMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages"); // name of the file without .properties extension
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true); // prevents null if key is missing
        return messageSource;
    }
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
            2,
            DistanceMetric.EUCLIDEAN,
            TaskLength.MEDIUM,
            2,
            1,
            1
        );
        List<DataPoint> mockPoints = List.of(
            new DataPoint(13, 87, 'A', null),
            new DataPoint(38, 73, 'B', null),
            new DataPoint(77, 3, 'C', null),
            new DataPoint(39, 3, 'D', null),
            new DataPoint(54, 19, 'E', null),
            new DataPoint(96, 68, 'F', null),
            new DataPoint(82, 7, 'G', null),
            new DataPoint(82, 80, 'H', null),
            new DataPoint(85, 36, 'I', null),
            new DataPoint(95, 69, 'J', null),
            new DataPoint(2, 34, 'K', null)
        );

        task.solve(mockPoints, createTestMessageSource());
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
            .body("numberOfClusters", equalTo(2));
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
            TaskLength.MEDIUM,
            2,
            1,
            1
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
            .body("descriptionEn", containsString("Perform the k-means algorithm"))
            .body("descriptionDe", containsString("Führen Sie den k-Means-Algorithmus"));
    }

    @Test
    void updateShouldReturnOk() {
        ModifyClusteringTaskDto updateDto = new ModifyClusteringTaskDto(
            4,
            DistanceMetric.MANHATTAN,
            TaskLength.LONG,
            2,
            1,
            1
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
