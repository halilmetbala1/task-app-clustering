package at.jku.dke.task_app.clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.DatabaseSetupExtension;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.DataPoint;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import at.jku.dke.task_app.clustering.logic.ClusteringTaskFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ExtendWith(DatabaseSetupExtension.class)
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private ClusteringTaskRepository taskRepository;
    private long taskId;
    private MessageSource createTestMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages"); // name of the file without .properties extension
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true); // prevents null if key is missing
        return messageSource;
    }
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        MessageSource ms = createTestMessageSource();

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


        ClusteringTask task = new ClusteringTask(1L, 2, DistanceMetric.EUCLIDEAN, TaskLength.MEDIUM, 2, 1, 1);
        task.setMaxPoints(BigDecimal.TEN);
        task.setStatus(TaskStatus.APPROVED);

        // Act
        task.solve(mockPoints, ms);

        task = taskRepository.save(task);
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
        assertEquals("Invalid input format.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateSubmitValid() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]\n" +
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals("3 of 3 iterations were evaluated correctly.", result.generalFeedback());
        assertEquals(3, result.criteria().size());
    }

    @Test
    void evaluateSubmitInvalidSyntax() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3, new ClusteringSubmissionDto("asdfasdf"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax Error")));
        assertEquals("Invalid input format.", result.generalFeedback());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertEquals(1, result.criteria().size());
    }

    @Test
    void evaluateDiagnoseInvalidSyntax() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-assignment", taskId,
            "de", SubmissionMode.DIAGNOSE, 3, new ClusteringSubmissionDto("asdfasdf"));

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertTrue(result.generalFeedback().contains("Schritte zur Lösung der Clustering-Aufgabe"));
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Visualisierung")));
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
        assertEquals("Invalid input format.", result.generalFeedback());
        assertEquals(1, result.criteria().size());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax Error") && x.feedback().contains("close to")));
    }
    @Test
    void evaluateFullFeedbackCorrectSolution() {
        // Arrange
        var input = "" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]\n" +
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3,
            new ClusteringSubmissionDto(input)
        );

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertEquals(BigDecimal.TEN.stripTrailingZeros(), result.maxPoints().stripTrailingZeros());
        assertTrue(result.criteria().size() >= 3);
    }

    @Test
    void evaluateSyntaxErrorInput() {
        // Arrange
        var input = "[(10,10) A, B]; [50,50): C, D]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3,
            new ClusteringSubmissionDto(input)
        );

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertTrue(result.criteria().stream().anyMatch(x -> x.name().equals("Syntax Error")));
    }

    @Test
    void evaluatePartialClusterError() {
        // Arrange
        var input = "[(10,10): A, B, C, D, E, F, G, H, I, J, K]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.points().compareTo(BigDecimal.TEN) < 0);
        assertTrue(result.criteria().stream().anyMatch(x -> x.feedback().contains("amount of clusters")));
    }

    @Test
    void evaluateDataPointAssignmentError() {
        // Arrange
        var input = "" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]\n" +
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J, K]; [(29.20,43.20): A, B, D, E]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        // Act
        var result = evaluationService.evaluate(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.points().compareTo(BigDecimal.TEN) < 0);
        assertTrue(result.criteria().stream().anyMatch(x -> x.feedback().contains("data point")));
        assertTrue(result.criteria().stream().anyMatch(x -> x.feedback().contains("assignment")));
    }
    @Test
    void evaluateTooFewIterations() {
        var input = "" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertTrue(result.points().compareTo(BigDecimal.TEN) < 0);
        assertTrue(result.generalFeedback().contains("evaluated correctly") || result.generalFeedback().contains("iterations"));
        assertTrue(result.criteria().stream().anyMatch(x -> x.feedback().contains("Wrong number of iterations")));
    }
    @Test
    void evaluateInvalidCentroidFormat() {
        var input ="[(85.00;36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]\n" +
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]"; // commas instead of points
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertTrue(result.generalFeedback().contains("Invalid input format"));
    }
    @Test
    void evaluateDuplicateLabels() {
        var input = "[(85.00,36.00): F, G, G, I, J]; [(54.00,19.00): A, B, C, D, E, K]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertTrue(result.generalFeedback().contains("Invalid input format"));
        assertTrue(result.criteria().stream().anyMatch(c -> c.feedback().toLowerCase().contains("duplicate")));
    }
    @Test
    void evaluateMissingLabel() {
        var input = "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D]"; // E, K missing
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertTrue(result.criteria().stream().anyMatch(x -> x.feedback().toLowerCase().contains("missing")));
    }
    @Test
    void evaluateUnknownLabel() {
        var input = "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, X, D, E, K]";
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 2,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.points().stripTrailingZeros());
        assertTrue(result.generalFeedback().contains("Invalid input format"));
        assertTrue(result.criteria().stream().anyMatch(x -> x.feedback().toLowerCase().contains("unknown")));
    }
    @Test
    void evaluate_with_extra_iteration() {
        //Input has one extra iteration (copied the last iteration again)
        String input = "" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]\n" +
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]\n" +
            "--\n" +  // extra iteration
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]";

        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3,
            new ClusteringSubmissionDto(input)
        );

        //Act
        var result = evaluationService.evaluate(dto);

        //Assert
        assertNotNull(result);
        assertTrue(result.points().compareTo(BigDecimal.TEN) < 0); // Should not get full points
        assertTrue(result.criteria().stream().anyMatch(c -> c.feedback().toLowerCase().contains("wrong number of iterations")));
        assertTrue(result.criteria().stream().anyMatch(c -> c.name().toLowerCase().contains("hint")));
    }

    @Test
    void evaluate_with_missing_centroid() {
        // Missing centroid in the second iteration
        String input = "" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.00,52.00): A, B, C, D, E, F, G, H, I, J, K]\n" +  // One centroid only
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]";

        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertTrue(result.points().compareTo(BigDecimal.TEN) < 0);
        assertTrue(result.criteria().stream().anyMatch(c -> c.feedback().toLowerCase().contains("amount of clusters")));
    }
    @Test
    void evaluate_with_wrong_centroid_coordinates() {
        // Centroid slightly off in the second iteration
        String input = "" +
            "[(85.00,36.00): F, G, H, I, J]; [(54.00,19.00): A, B, C, D, E, K]\n" +
            "--\n" +
            "[(88.50,51.40): C, F, G, H, I, J]; [(37.17,36.50): A, B, D, E, K]\n" + // Wrong centroid
            "--\n" +
            "[(86.17,43.83): C, F, G, H, I, J]; [(29.20,43.20): A, B, D, E, K]";

        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>(
            "test-user", "test-assignment", taskId,
            "en", SubmissionMode.SUBMIT, 3,
            new ClusteringSubmissionDto(input)
        );

        var result = evaluationService.evaluate(dto);

        assertNotNull(result);
        assertTrue(result.points().compareTo(BigDecimal.TEN) < 0);
        assertTrue(result.criteria().stream().anyMatch(c -> c.feedback().toLowerCase().contains("centroid")));
    }
}
