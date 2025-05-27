package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ClusteringTaskTest {

    @Test
    void testConstructorFull() {
        // Arrange
        final int numberOfClusters = 3;
        final BigDecimal maxPoints = BigDecimal.TEN;
        final TaskStatus status = TaskStatus.APPROVED;
        final DistanceMetric distanceMetric = DistanceMetric.EUCLIDEAN;
        final DifficultyLevel difficulty = DifficultyLevel.EASY;
        final Long id = 99L;

        // Act
        var task = new ClusteringTask(id, maxPoints, status,
            numberOfClusters, distanceMetric, difficulty);

        // Assert
        assertEquals(id, task.getId());
        assertEquals(numberOfClusters, task.getNumberOfClusters());
        //assertEquals(numberOfDataPoints, task.getNumberOfDataPoints());
        assertEquals(maxPoints, task.getMaxPoints());
        assertEquals(status, task.getStatus());
        assertEquals(distanceMetric, task.getDistanceMetric());
        assertEquals(difficulty, task.getDifficulty());
    }

    @Test
    void testGetSetNumberOfClusters() {
        var task = new ClusteringTask();
        int expected = 4;
        task.setNumberOfClusters(expected);
        assertEquals(expected, task.getNumberOfClusters());
    }

    @Test
    void testGetSetNumberOfDataPoints() {
        var task = new ClusteringTask();
        int expected = 50;
        task.setNumberOfDataPoints(expected);
        assertEquals(expected, task.getNumberOfDataPoints());
    }

    @Test
    void testGenerateAndSolve() {
        var task = new ClusteringTask(
            BigDecimal.TEN,
            TaskStatus.APPROVED,
            3,
            DistanceMetric.EUCLIDEAN,
            DifficultyLevel.EASY);

        //ClusteringTaskGenerator.generateAndSolve(task);

        assertNotNull(task.getClusters());
        assertFalse(task.getClusters().isEmpty());
    }
}
