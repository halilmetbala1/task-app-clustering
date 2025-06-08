package at.jku.dke.task_app.clustering.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.dto.ClusteringTaskDto;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import at.jku.dke.task_app.clustering.services.ClusteringTaskService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link ClusteringTask}s.
 */
@RestController
public class TaskController extends BaseTaskController<ClusteringTask, ClusteringTaskDto, ModifyClusteringTaskDto> {

    /**
     * Creates a new instance of class {@link TaskController}.
     *
     * @param taskService The task service.
     */
    public TaskController(ClusteringTaskService taskService) {
        super(taskService);
    }
    @Override
    protected ClusteringTaskDto mapToDto(ClusteringTask task) {
        return fromTask(task);
    }

    public static ClusteringTaskDto fromTask(ClusteringTask task) {

        return new ClusteringTaskDto(
            task.getNumberOfClusters(),
            task.getNumberOfDataPoints(),
            task.getDistanceMetric(),
            task.getTaskLength()
        );
    }
}
