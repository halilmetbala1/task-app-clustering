package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskGroupControllerTest {

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
