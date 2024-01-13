package at.jku.dke.task_app.binary_search.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.controllers.BaseTaskGroupController;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import at.jku.dke.task_app.binary_search.dto.BinarySearchTaskGroupDto;
import at.jku.dke.task_app.binary_search.services.BinarySearchTaskGroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    /**
     * Returns two random numbers.
     * <p>
     * This method is used to demonstrate how additional endpoints can be used.
     *
     * @return Two random numbers.
     */
    @GetMapping(value = "/random", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize(AuthConstants.CRUD_AUTHORITY)
    @SecurityRequirement(name = AuthConstants.API_KEY_REQUIREMENT)
    public ResponseEntity<Serializable> getRandomNumbers() {
        var rand = new Random();
        var min = rand.nextInt(100);
        var map = Map.of("min", min, "max", rand.nextInt(min + 1, 1000));
        return ResponseEntity.ok(new HashMap<>(map));
    }

}
