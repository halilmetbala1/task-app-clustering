package at.jku.dke.task_app.clustering.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.ClientSetupExtension;
import at.jku.dke.task_app.clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.clustering.data.entities.ClusteringSubmission;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringSubmissionRepository;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class SubmissionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ClusteringTaskRepository repository;

    @Autowired
    private ClusteringSubmissionRepository submissionRepository;

    private long taskId;
    private UUID ungraded;
    private UUID graded;

    @BeforeEach
    void initDb() {
        this.repository.deleteAll();

        var task = this.repository.save(new ClusteringTask(1L,
            BigDecimal.TWO,
            TaskStatus.APPROVED,
            3,
            DistanceMetric.EUCLIDEAN,
            TaskLength.SHORT,
            2,
            1,
            1
        ));
        this.taskId = task.getId();

        var submission = new ClusteringSubmission("test-user", "test-id", task, "de", 3, SubmissionMode.SUBMIT, "5");
        submission.setEvaluationResult(new GradingDto(BigDecimal.TWO, BigDecimal.TWO, "success", new ArrayList<>()));
        this.graded = this.submissionRepository.save(submission).getId();
        this.ungraded = this.submissionRepository.save(new ClusteringSubmission("test-user", "test-id", task, "de", 3, SubmissionMode.SUBMIT, "5")).getId();
    }

    //#region --- SUBMIT ---
    @Test
    void submitInForegroundWithPersist() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new SubmitSubmissionDto<>("test-user", "test-id", this.taskId, "de", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("2")))
            // WHEN
            .when()
            .post("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .header("Location", containsString("/api/submission/"))
            .body("submissionId", hasLength(36))
            .body("grading.maxPoints", equalTo(2f))
            .body("grading.points", equalTo(0))
            .body("grading.generalFeedback", any(String.class))
            .body("grading.criteria", hasSize(1));
    }

    @Test
    void submitInForegroundWithoutPersist() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .queryParams("persist", false)
            .contentType(ContentType.JSON)
            .body(new SubmitSubmissionDto<>("test-user", "test-id", this.taskId, "de",
                SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("5")))
            // WHEN
            .when()
            .post("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("submissionId", nullValue())
            .body("grading.maxPoints", equalTo(2f))
            .body("grading.points", equalTo(0))
            .body("grading.generalFeedback", any(String.class))
            .body("grading.criteria", hasSize(1));
    }

    @Test
    void submitInBackground() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .queryParams("runInBackground", true)
            .body(new SubmitSubmissionDto<>("test-user", "test-id", this.taskId, "de", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("5")))
            // WHEN
            .when()
            .post("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(202)
            .contentType(ContentType.TEXT)
            .header("Location", containsString("/api/submission/"))
            .body(any(String.class))
            .body(hasLength(36));
    }

    @Test
    void submitShouldReturnBadRequest() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new SubmitSubmissionDto<>("test-user", "test-id", this.taskId, "it", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("5")))
            // WHEN
            .when()
            .post("/api/submission")
            // THEN
            .then()
            .statusCode(400);
    }

    @Test
    void submitShouldReturnBadRequestOnInvalidTaskId() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new SubmitSubmissionDto<>("test-user", "test-id", this.taskId + 1, "de", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("5")))
            // WHEN
            .when()
            .post("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void submitShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new SubmitSubmissionDto<>("test-user", "test-id", this.taskId, "de", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("5")))
            // WHEN
            .when()
            .post("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }

    @Test
    void submitParallel() {
        var threads = IntStream.range(0, 100)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                given()
                    .port(port)
                    .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
                    .contentType(ContentType.JSON)
                    .body(new SubmitSubmissionDto<>("test-user-" + i, "test-id", this.taskId, "de", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("5")))
                    // WHEN
                    .when()
                    .post("/api/submission")
                    // THEN
                    .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .header("Location", containsString("/api/submission/"))
                    .body("submissionId", hasLength(36));
                return true;
            }))
            .toList();
        threads.stream().map(CompletableFuture::join).forEach(Assertions::assertTrue);
    }
    //#endregion

    //#region --- GET ALL ---
    @Test
    void getAllShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.READ_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("totalElements", equalTo(2))
            .body("content", hasSize(2))
            .body("content", everyItem(hasKey("id")));
    }

    @Test
    void getAllShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/submission")
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- GET ---
    @Test
    void getReturnsOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            .header("X-API-TIMEOUT", 10)
            // WHEN
            .when()
            .get("/api/submission/{id}/result", this.graded.toString())
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("maxPoints", equalTo(2))
            .body("points", equalTo(2))
            .body("generalFeedback", any(String.class))
            .body("criteria", hasSize(0));
    }

    @Test
    void getReturnsOkAfterWait() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);

                var submission = this.submissionRepository.findById(this.ungraded).orElseThrow();
                submission.setEvaluationResult(new GradingDto(BigDecimal.TWO, BigDecimal.ZERO, "failed", new ArrayList<>()));
                this.submissionRepository.saveAndFlush(submission);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }).start();

        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            .header("X-API-TIMEOUT", 10)
            // WHEN
            .when()
            .get("/api/submission/{id}/result", this.ungraded.toString())
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("maxPoints", equalTo(2))
            .body("points", equalTo(0))
            .body("generalFeedback", any(String.class))
            .body("criteria", hasSize(0))
            .time(greaterThan(3000L))
            .time(lessThan(12000L));
    }

    @Test
    void getReturnsOkWithDelete() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            .header("X-API-TIMEOUT", 10)
            .queryParams("delete", true)
            // WHEN
            .when()
            .get("/api/submission/{id}/result", this.graded.toString())
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("maxPoints", equalTo(2))
            .body("points", equalTo(2))
            .body("generalFeedback", any(String.class))
            .body("criteria", hasSize(0));
        assertFalse(this.submissionRepository.existsById(this.graded));
    }

    @Test
    void getReturnsTimeout() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            .header("X-API-TIMEOUT", 3)
            // WHEN
            .when()
            .get("/api/submission/{id}/result", this.ungraded.toString())
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(408);
    }

    @Test
    void getReturnsNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            .header("X-API-TIMEOUT", 10)
            // WHEN
            .when()
            .get("/api/submission/{id}/result", UUID.randomUUID().toString())
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void getReturnsForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            .header("X-API-TIMEOUT", 10)
            // WHEN
            .when()
            .get("/api/submission/{id}/result", UUID.randomUUID().toString())
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion
}
