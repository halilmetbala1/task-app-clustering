package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BinarySearchTaskServiceTest {

    @Test
    void createTask() {
        // Arrange
        ModifyTaskDto<ModifyBinarySearchTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(33));
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, null);

        // Act
        BinarySearchTask task = service.createTask(3, dto);

        // Assert
        assertEquals(dto.additionalData().solution(), task.getSolution());
    }

    @Test
    void createTaskInvalidType() {
        // Arrange
        ModifyTaskDto<ModifyBinarySearchTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(33));
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTask(3, dto));
    }

    @Test
    void updateTask() {
        // Arrange
        ModifyTaskDto<ModifyBinarySearchTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "binary-search", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(33));
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, null);
        BinarySearchTask task = new BinarySearchTask(3);

        // Act
        service.updateTask(task, dto);

        // Assert
        assertEquals(dto.additionalData().solution(), task.getSolution());
    }

    @Test
    void updateTaskInvalidType() {
        // Arrange
        ModifyTaskDto<ModifyBinarySearchTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyBinarySearchTaskDto(33));
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, null);
        BinarySearchTask task = new BinarySearchTask(3);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTask(task, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        MessageSource ms = mock(MessageSource.class);
        BinarySearchTaskService service = new BinarySearchTaskService(null, null, ms);
        BinarySearchTask task = new BinarySearchTask(3);
        task.setSolution(33);

        // Act
        TaskModificationResponseDto result = service.mapToReturnData(task, true);

        // Assert
        assertNotNull(result);
        verify(ms).getMessage("defaultTaskDescription", null, Locale.GERMAN);
        verify(ms).getMessage("defaultTaskDescription", null, Locale.ENGLISH);
    }

}
