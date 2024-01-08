package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskGroupController;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import at.jku.dke.task_app.binary_search.dto.BinarySearchTaskGroupDto;
import at.jku.dke.task_app.binary_search.services.BinarySearchTaskGroupService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link BinarySearchTaskGroup}s.
 */
@RestController
public class TaskGroupController extends BaseTaskGroupController<BinarySearchTaskGroup, BinarySearchTaskGroupDto, ModifyBinarySearchTaskGroupDto> {

    /**
     * Creates a new instance of class {@link TaskGroupController}.
     *
     * @param taskGroupService The task group service.
     */
    public TaskGroupController(BinarySearchTaskGroupService taskGroupService) {
        super(taskGroupService);
    }

    @Override
    protected BinarySearchTaskGroupDto mapToDto(BinarySearchTaskGroup taskGroup) {
        return new BinarySearchTaskGroupDto(taskGroup.getMinNumber(), taskGroup.getMaxNumber());
    }

}
