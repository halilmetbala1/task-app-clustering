package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchTaskGroupServiceTest {

    @Test
    void createTaskGroup() {
        // Arrange
        ModifyTaskGroupDto<ModifyBinarySearchTaskGroupDto> dto = new ModifyTaskGroupDto<>("binary_search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 2));
        BinarySearchTaskGroupService service = new BinarySearchTaskGroupService(null, null);

        // Act
        var taskGroup = service.createTaskGroup(3, dto);

        // Assert
        assertEquals(dto.additionalData().minNumber(), taskGroup.getMinNumber());
        assertEquals(dto.additionalData().maxNumber(), taskGroup.getMaxNumber());
    }

    @Test
    void updateTaskGroup() {
        // Arrange
        ModifyTaskGroupDto<ModifyBinarySearchTaskGroupDto> dto = new ModifyTaskGroupDto<>("binary_search", TaskStatus.APPROVED, new ModifyBinarySearchTaskGroupDto(1, 2));
        BinarySearchTaskGroupService service = new BinarySearchTaskGroupService(null, null);
        var taskGroup = new BinarySearchTaskGroup(3, 4);

        // Act
        service.updateTaskGroup(taskGroup, dto);

        // Assert
        assertEquals(dto.additionalData().minNumber(), taskGroup.getMinNumber());
        assertEquals(dto.additionalData().maxNumber(), taskGroup.getMaxNumber());
    }

}
