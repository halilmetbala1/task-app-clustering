package at.jku.dke.task_app.binary_search.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTaskGroupTest {

    @Test
    void testConstructor1() {
        // Arrange
        final int expectedMinNumber = 21;
        final int expectedMaxNumber = 42;

        // Act
        BinarySearchTaskGroup binarySearchTaskGroup = new BinarySearchTaskGroup(expectedMinNumber, expectedMaxNumber);
        int actualMinNumber = binarySearchTaskGroup.getMinNumber();
        int actualMaxNumber = binarySearchTaskGroup.getMaxNumber();

        // Assert
        assertEquals(expectedMinNumber, actualMinNumber);
        assertEquals(expectedMaxNumber, actualMaxNumber);
    }

    @Test
    void testConstructor2() {
        // Arrange
        final TaskStatus status = TaskStatus.APPROVED;
        final int expectedMinNumber = 21;
        final int expectedMaxNumber = 42;

        // Act
        BinarySearchTaskGroup binarySearchTaskGroup = new BinarySearchTaskGroup(status, expectedMinNumber, expectedMaxNumber);
        TaskStatus actualStatus = binarySearchTaskGroup.getStatus();
        int actualMinNumber = binarySearchTaskGroup.getMinNumber();
        int actualMaxNumber = binarySearchTaskGroup.getMaxNumber();

        // Assert
        assertEquals(status, actualStatus);
        assertEquals(expectedMinNumber, actualMinNumber);
        assertEquals(expectedMaxNumber, actualMaxNumber);
    }

    @Test
    void testConstructor3() {
        // Arrange
        final long expectedId = 21;
        final TaskStatus status = TaskStatus.APPROVED;
        final int expectedMinNumber = 21;
        final int expectedMaxNumber = 42;

        // Act
        BinarySearchTaskGroup binarySearchTaskGroup = new BinarySearchTaskGroup(expectedId, status, expectedMinNumber, expectedMaxNumber);
        long actualId = binarySearchTaskGroup.getId();
        TaskStatus actualStatus = binarySearchTaskGroup.getStatus();
        int actualMinNumber = binarySearchTaskGroup.getMinNumber();
        int actualMaxNumber = binarySearchTaskGroup.getMaxNumber();

        // Assert
        assertEquals(expectedId, actualId);
        assertEquals(status, actualStatus);
        assertEquals(expectedMinNumber, actualMinNumber);
        assertEquals(expectedMaxNumber, actualMaxNumber);
    }

    @Test
    void testGetSetMinNumber() {
        // Arrange
        BinarySearchTaskGroup binarySearchTaskGroup = new BinarySearchTaskGroup();
        final int expected = 21;

        // Act
        binarySearchTaskGroup.setMinNumber(expected);
        int actual = binarySearchTaskGroup.getMinNumber();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testGetSetMaxNumber() {
        // Arrange
        BinarySearchTaskGroup binarySearchTaskGroup = new BinarySearchTaskGroup();
        final int expected = 21;

        // Act
        binarySearchTaskGroup.setMaxNumber(expected);
        int actual = binarySearchTaskGroup.getMaxNumber();

        // Assert
        assertEquals(expected, actual);
    }

}
