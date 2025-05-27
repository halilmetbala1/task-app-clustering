package at.jku.dke.task_app.clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(DatabaseSetupExtension.class)
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private ClusteringTaskRepository taskRepository;
    private long taskId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        var task = taskRepository.save(new ClusteringTask(1L,
            BigDecimal.TWO,
            TaskStatus.APPROVED,
            3,
            DistanceMetric.EUCLIDEAN,
            DifficultyLevel.EASY
        ));
        this.taskId = task.getId();
    }

    @Test
    void evaluateRun() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.RUN, 3, new ClusteringSubmissionDto("20"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your Input: 20", result.generalFeedback());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateSubmitValid() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("20"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is correct.", result.generalFeedback());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateSubmitInvalidSyntax() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("20 jku"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax")));
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateSubmitTooSmall() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("18"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateDiagnoseValid() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.DIAGNOSE, 3, new ClusteringSubmissionDto("20"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is correct.", result.generalFeedback());
        assertEquals(2, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Value") && x.feedback().equals("You have found the searched number.")));
    }

    @Test
    void evaluateDiagnoseInvalidSyntax() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.DIAGNOSE, 3, new ClusteringSubmissionDto("20 test"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax")));
    }

    @Test
    void evaluateDiagnoseTooBig() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.DIAGNOSE, 3, new ClusteringSubmissionDto("25"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(2, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Value") && x.feedback().equals("The searched number is smaller.")));
    }

    @Test
    void evaluateDiagnoseTooSmall() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.DIAGNOSE, 3, new ClusteringSubmissionDto("15"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(2, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Value") && x.feedback().equals("The searched number is bigger.")));
    }


    @Test
    void evaluateDiagnoseNoFeedback() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.DIAGNOSE, 0, new ClusteringSubmissionDto("15"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("Your solution is incorrect.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax") && x.feedback().equals("Valid Number")));
    }
}
