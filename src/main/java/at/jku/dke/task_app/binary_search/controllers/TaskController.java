package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.dto.BinarySearchTaskDto;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import at.jku.dke.task_app.binary_search.services.BinarySearchTaskService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link BinarySearchTask}s.
 */
@RestController
public class TaskController extends BaseTaskController<BinarySearchTask, BinarySearchTaskDto, ModifyBinarySearchTaskDto> {

    /**
     * Creates a new instance of class {@link TaskController}.
     *
     * @param taskService The task service.
     */
    public TaskController(BinarySearchTaskService taskService) {
        super(taskService);
    }

    @Override
    protected BinarySearchTaskDto mapToDto(BinarySearchTask task) {
        return new BinarySearchTaskDto(task.getSolution());
    }

}
