package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskControllerTest {

    @Test
    void mapToDto() {
        // Arrange
        var task = new BinarySearchTask(42);

        // Act
        var result = new TaskController(null).mapToDto(task);

        // Assert
        assertEquals(42, result.solution());
    }

}
