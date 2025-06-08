package at.jku.dke.task_app.clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClusteringTaskServiceTest {
    private MessageSource createTestMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages"); // refers to messages_en.properties etc.
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
    @Test
    void createTask() {
        // Arrange
        var modifyDto = new ModifyClusteringTaskDto(3, DistanceMetric.EUCLIDEAN, TaskLength.SHORT, 2, 1, 1);
        ModifyTaskDto<ModifyClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "clustering", TaskStatus.APPROVED, modifyDto);

        MessageSource messageSource = createTestMessageSource();
        ClusteringTaskService service = new ClusteringTaskService(null, messageSource);

        // Act
        ClusteringTask task = service.createTask(7, dto);

        // Assert
        assertEquals(3, task.getNumberOfClusters());
        assertTrue(task.getNumberOfDataPoints() > 0, "Data point count should be greater than 0");
        assertNotNull(task.getSolutionInstructionsDe());
        assertNotNull(task.getSolutionInstructionsEn());
        assertFalse(task.getSolutionIterations().isEmpty(), "Solution iterations should not be empty");
    }

    @Test
    void createTaskInvalidType() {
        // Arrange
        var modifyDto = new ModifyClusteringTaskDto(3, DistanceMetric.EUCLIDEAN, TaskLength.SHORT, 2, 2, 1);
        ModifyTaskDto<ModifyClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, modifyDto);
        ClusteringTaskService service = new ClusteringTaskService(null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTask(3, dto));
    }

    @Test
    void updateTaskInvalidType() {
        // Arrange
        var modifyDto = new ModifyClusteringTaskDto(3, DistanceMetric.EUCLIDEAN, TaskLength.SHORT, 2, 2, 1);
        ModifyTaskDto<ModifyClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, modifyDto);
        ClusteringTaskService service = new ClusteringTaskService(null, null);
        ClusteringTask task = new ClusteringTask();

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTask(task, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        ClusteringTaskRepository mockRepo = mock(ClusteringTaskRepository.class);
        MessageSource ms = createTestMessageSource();
        ClusteringTaskService service = new ClusteringTaskService(mockRepo, ms);
        ClusteringTask task = new ClusteringTask();

        // Act
        TaskModificationResponseDto result = service.mapToReturnData(task, true);

        // Assert
        assertNotNull(result);
        assertNotNull(result.descriptionDe());
        assertFalse(result.descriptionDe().isBlank());
        assertNotNull(result.descriptionEn());
        assertFalse(result.descriptionEn().isBlank());
    }
}
