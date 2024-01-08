package at.jku.dke.task_app.binary_search.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTaskTest {

    @Test
    void testConstructor1() {
        // Arrange
        final int expected = 42;

        // Act
        var task = new BinarySearchTask(expected);
        int actual = task.getSolution();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testConstructor2() {
        // Arrange
        final int expected = 42;
        final BigDecimal maxPoints = BigDecimal.TEN;
        final TaskStatus status = TaskStatus.APPROVED;
        final BinarySearchTaskGroup taskGroup = new BinarySearchTaskGroup();
        taskGroup.setId(55L);

        // Act
        var task = new BinarySearchTask(maxPoints, status, taskGroup, expected);
        int actualSolution = task.getSolution();
        BigDecimal actualMaxPoints = task.getMaxPoints();
        TaskStatus actualStatus = task.getStatus();
        BinarySearchTaskGroup actualTaskGroup = task.getTaskGroup();

        // Assert
        assertEquals(expected, actualSolution);
        assertEquals(maxPoints, actualMaxPoints);
        assertEquals(status, actualStatus);
        assertEquals(taskGroup, actualTaskGroup);
    }

    @Test
    void testConstructor3() {
        // Arrange
        final int expected = 42;
        final BigDecimal maxPoints = BigDecimal.TEN;
        final TaskStatus status = TaskStatus.APPROVED;
        final BinarySearchTaskGroup taskGroup = new BinarySearchTaskGroup();
        taskGroup.setId(55L);
        final long id = 1L;

        // Act
        var task = new BinarySearchTask(id, maxPoints, status, taskGroup, expected);
        long actualId = task.getId();
        int actualSolution = task.getSolution();
        BigDecimal actualMaxPoints = task.getMaxPoints();
        TaskStatus actualStatus = task.getStatus();
        BinarySearchTaskGroup actualTaskGroup = task.getTaskGroup();

        // Assert
        assertEquals(id, actualId);
        assertEquals(expected, actualSolution);
        assertEquals(maxPoints, actualMaxPoints);
        assertEquals(status, actualStatus);
        assertEquals(taskGroup, actualTaskGroup);
    }

    @Test
    void testGetSetSolution() {
        // Arrange
        var task = new BinarySearchTask();
        final int expected = 42;

        // Act
        task.setSolution(expected);
        final int actual = task.getSolution();

        // Assert
        assertEquals(expected, actual);
    }

}
