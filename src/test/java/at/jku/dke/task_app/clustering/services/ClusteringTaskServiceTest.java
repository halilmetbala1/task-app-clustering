package at.jku.dke.task_app.clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import at.jku.dke.task_app.clustering.logic.ClusteringTaskFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClusteringTaskServiceTest {

    @Test
    void createTask() {
        // Arrange
        var modifyDto = new ModifyClusteringTaskDto(3, DistanceMetric.EUCLIDEAN, DifficultyLevel.EASY);
        ModifyTaskDto<ModifyClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "clustering", TaskStatus.APPROVED, modifyDto);
        ClusteringTaskService service = new ClusteringTaskService(null, null);

        // Act
        ClusteringTask task = service.createTask(7, dto);

        // Assert
        assertEquals(3, task.getNumberOfClusters());
        assertEquals(30, task.getNumberOfDataPoints());
    }

    @Test
    void createTaskInvalidType() {
        // Arrange
        var modifyDto = new ModifyClusteringTaskDto(3, DistanceMetric.EUCLIDEAN, DifficultyLevel.EASY);
        ModifyTaskDto<ModifyClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, modifyDto);
        ClusteringTaskService service = new ClusteringTaskService(null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTask(3, dto));
    }

    @Test
    void updateTaskInvalidType() {
        // Arrange
        var modifyDto = new ModifyClusteringTaskDto(3, DistanceMetric.EUCLIDEAN, DifficultyLevel.EASY);
        ModifyTaskDto<ModifyClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, modifyDto);
        ClusteringTaskService service = new ClusteringTaskService(null, null);
        ClusteringTask task = new ClusteringTask();

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTask(task, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        MessageSource ms = mock(MessageSource.class);
        ClusteringTaskService service = new ClusteringTaskService(null, ms);
        ClusteringTask task = new ClusteringTask();

        // Act
        TaskModificationResponseDto result = service.mapToReturnData(task, true);

        // Assert
        assertNotNull(result);
        verify(ms).getMessage("defaultTaskDescription", null, Locale.GERMAN);
        verify(ms).getMessage("defaultTaskDescription", null, Locale.ENGLISH);
    }
}
