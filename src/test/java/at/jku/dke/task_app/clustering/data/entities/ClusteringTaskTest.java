package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.dto.PointDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ClusteringTaskTest {

    private ClusteringTask task;

    @BeforeEach
    void setUp() {
        task = new ClusteringTask(
            BigDecimal.TEN,
            TaskStatus.APPROVED,
            3,
            DistanceMetric.EUCLIDEAN,
            TaskLength.SHORT
        );
    }

    @Test
    void testConstructorFull() {
        var fullTask = new ClusteringTask(99L, BigDecimal.TEN, TaskStatus.APPROVED,
            3, DistanceMetric.EUCLIDEAN, TaskLength.MEDIUM,
            2, 1, 1);

        assertEquals(99L, fullTask.getId());
        assertEquals(3, fullTask.getNumberOfClusters());
        assertEquals(BigDecimal.TEN, fullTask.getMaxPoints());
        assertEquals(TaskStatus.APPROVED, fullTask.getStatus());
        assertEquals(DistanceMetric.EUCLIDEAN, fullTask.getDistanceMetric());
        assertEquals(TaskLength.MEDIUM, fullTask.getTaskLength());
        assertEquals(2, fullTask.getDeductionWrongClusters());
        assertEquals(1, fullTask.getDeductionWrongLabels());
        assertEquals(1, fullTask.getDeductionWrongCentroids());
    }

    @Test
    void testSetGetNumberOfClusters() {
        task.setNumberOfClusters(4);
        assertEquals(4, task.getNumberOfClusters());
    }

    @Test
    void testSetGetNumberOfDataPoints() {
        task.setNumberOfDataPoints(50);
        assertEquals(50, task.getNumberOfDataPoints());
    }

    @Test
    void testSetGetDistanceMetric() {
        task.setDistanceMetric(DistanceMetric.MANHATTAN);
        assertEquals(DistanceMetric.MANHATTAN, task.getDistanceMetric());
    }

    @Test
    void testSetGetTaskLength() {
        task.setTaskLength(TaskLength.LONG);
        assertEquals(TaskLength.LONG, task.getTaskLength());
    }

    @Test
    void testSetGetDeductionValues() {
        task.setDeductionWrongClusters(2);
        task.setDeductionWrongLabels(3);
        task.setDeductionWrongCentroids(4);

        assertEquals(2, task.getDeductionWrongClusters());
        assertEquals(3, task.getDeductionWrongLabels());
        assertEquals(4, task.getDeductionWrongCentroids());
    }

    @Test
    void testExerciseDataPointsAsStringSorted() {
        List<DataPoint> points = List.of(
            new DataPoint(10, 10, 'B', null),
            new DataPoint(5, 5, 'A', null)
        );

        Cluster cluster = new Cluster();
        cluster.setType(at.jku.dke.task_app.clustering.data.entities.enums.ClusterType.EXERCISE);
        cluster.getDataPoints().addAll(points);

        task.getClusters().add(cluster);

        String result = task.getExerciseDataPointsAsString();
        assertTrue(result.startsWith("A(5,00"));
    }

    @Test
    void testExerciseCentroidsAsString() {
        DataPoint p1 = new DataPoint(20, 20, 'X', null);
        Cluster cluster = new Cluster();
        cluster.setType(at.jku.dke.task_app.clustering.data.entities.enums.ClusterType.EXERCISE);
        cluster.setX(20);
        cluster.setY(20);
        cluster.setName("C1");
        cluster.getDataPoints().add(p1);

        task.getClusters().add(cluster);
        String result = task.getExerciseCentroidsAsString();
        assertEquals("C1: X", result);
    }

    @Test
    void testGetSolutionInstructionsLocalized() {
        task.setSolutionInstructionsDe("DE");
        task.setSolutionInstructionsEn("EN");

        assertEquals("EN", task.getSolutionInstructions(java.util.Locale.ENGLISH));
        assertEquals("DE", task.getSolutionInstructions(java.util.Locale.GERMAN));
    }

    @Test
    void testGetSolutionIterationsString() {
        SolutionIteration s1 = new SolutionIteration(0, "[Iteration 1]", task);
        SolutionIteration s2 = new SolutionIteration(1, "[Iteration 2]", task);
        task.setSolutionIterations(List.of(s1, s2));

        String result = task.getSolutionIterationsString();
        assertTrue(result.contains("[Iteration 1]"));
        assertTrue(result.contains("--"));
        assertTrue(result.contains("[Iteration 2]"));
    }

    @Test
    void testGenerateExercisePointsRespectsBounds() {
        task.setTaskLength(TaskLength.SHORT);
        List<DataPoint> points = task.generateExercisePoints();
        assertTrue(points.size() >= 5 && points.size() <= 8);
    }

    @Test
    void testAssignCentroidsByTaskLengthShortRandomness() {
        List<DataPoint> inputPoints = task.generateExercisePoints();
        List<PointDto> centroids = task.assignCentroidsByTaskLength(inputPoints);

        assertEquals(task.getNumberOfClusters(), centroids.size());
    }
    @Test
    void testSolveWithMockPoints() {
        // Arrange
        ClusteringTask task = new ClusteringTask(BigDecimal.TEN, TaskStatus.APPROVED, 3, DistanceMetric.EUCLIDEAN, TaskLength.SHORT);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        // Mock translations (minimal for functionality)
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.eq(Locale.GERMAN))).thenReturn("de");
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.eq(Locale.ENGLISH))).thenReturn("en");

        List<DataPoint> mockPoints = List.of(
            new DataPoint(49, 97, 'A', null),
            new DataPoint(53, 5, 'B', null),
            new DataPoint(33, 65, 'C', null),
            new DataPoint(62, 51, 'D', null),
            new DataPoint(100, 38, 'E', null),
            new DataPoint(61, 45, 'F', null),
            new DataPoint(74, 27, 'G', null),
            new DataPoint(64, 17, 'H', null),
            new DataPoint(36, 17, 'I', null),
            new DataPoint(96, 12, 'J', null)
        );

        // Act
        task.solve(mockPoints, messageSource);

        // Assert
        assertNotNull(task.getSolutionInstructionsDe());
        assertNotNull(task.getSolutionInstructionsEn());
        assertFalse(task.getSolutionClusters().isEmpty());
        assertFalse(task.getExerciseClusters().isEmpty());
        assertFalse(task.getSolutionIterations().isEmpty());
    }
    @Test
    void testSolveAndVerifySolutionClusters() {
        // Arrange
        ClusteringTask task = new ClusteringTask(BigDecimal.TEN, TaskStatus.APPROVED,
            2, DistanceMetric.EUCLIDEAN, TaskLength.MEDIUM);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        // Mock translations (minimal stubs to avoid NPEs)
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.eq(Locale.GERMAN))).thenReturn("de");
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.eq(Locale.ENGLISH))).thenReturn("en");


        List<DataPoint> inputPoints = List.of(
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

        // Act
        task.solve(inputPoints, messageSource);

        // Assert
        List<Cluster> solClusters = task.getSolutionClusters();
        assertEquals(2, solClusters.size(), "Should have exactly 2 solution clusters.");

        int totalAssignedPoints = solClusters.stream()
            .mapToInt(c -> c.getDataPoints().size())
            .sum();

        assertEquals(inputPoints.size(), totalAssignedPoints, "All points should be assigned to clusters.");

        for (Cluster cluster : solClusters) {
            assertNotNull(cluster.getName());
            assertTrue(cluster.getX() >= 0 && cluster.getX() <= 100);
            assertTrue(cluster.getY() >= 0 && cluster.getY() <= 100);
            assertFalse(cluster.getDataPoints().isEmpty(), "Cluster should contain at least one point.");
        }

        assertNotNull(task.getSolutionInstructionsDe());
        assertNotNull(task.getSolutionInstructionsEn());
        assertFalse(task.getSolutionIterations().isEmpty(), "There should be iterations recorded.");
    }
}
