package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchTaskServiceTest {

    @Test
    void createTask() {
        // Arrange
        ModifyTaskDto<ModifyBinarySearchTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "binary_search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(33));
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, null);

        // Act
        BinarySearchTask task = service.createTask(3, dto);

        // Assert
        assertEquals(dto.additionalData().solution(), task.getSolution());
    }

    @Test
    void updateTask() {
        // Arrange
        ModifyTaskDto<ModifyBinarySearchTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "binary_search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(33));
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, null);
        BinarySearchTask task = new BinarySearchTask(3);

        // Act
        service.updateTask(task, dto);

        // Assert
        assertEquals(dto.additionalData().solution(), task.getSolution());
    }

}
