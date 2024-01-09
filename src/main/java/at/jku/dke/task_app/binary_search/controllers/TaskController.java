package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.dto.BinarySearchTaskDto;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import at.jku.dke.task_app.binary_search.services.BinarySearchTaskService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    /**
     * Returns two random numbers.
     * <p>
     * This method is used to demonstrate how additional endpoints can be used.
     *
     * @return Two random numbers.
     */
    @GetMapping(value = "/random", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize(AuthConstants.CRUD_AUTHORITY)
    public ResponseEntity<Serializable> getRandomNumbers() {
        var rand = new Random();
        var min = rand.nextInt(100);
        var map = Map.of("min", min, "max", rand.nextInt(min + 1, 1000));
        return ResponseEntity.ok(new HashMap<>(map));
    }

}
