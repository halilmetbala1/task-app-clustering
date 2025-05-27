package at.jku.dke.task_app.clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.clustering.data.entities.*;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import at.jku.dke.task_app.clustering.logic.ClusteringImageGeneration;
import at.jku.dke.task_app.clustering.logic.ClusteringTaskFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * This class provides methods for managing {@link ClusteringTask}s.
 */
@Service
public class ClusteringTaskService extends BaseTaskService<ClusteringTask, ModifyClusteringTaskDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link ClusteringTaskService}.
     *
     * @param repository          The task repository.
     * @param messageSource       The message source.
     */
    public ClusteringTaskService(ClusteringTaskRepository repository, MessageSource messageSource) {
        super(repository);
        this.messageSource = messageSource;
    }
//    public ClusteringTask save(ModifyClusteringTaskDto dto) {
//        ClusteringTask task = new ClusteringTask();
//        task.setNumberOfClusters(dto.numberOfClusters());
//        task.setNumberOfDataPoints(dto.numberOfDataPoints());
//        task.setDistanceMetric(dto.distanceMetric());
//        task.setDifficulty(dto.difficulty());
//
//
//        return repository.save(task);
//    }

    @Override
    protected ClusteringTask createTask(long id, ModifyTaskDto<ModifyClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        ModifyClusteringTaskDto dto = modifyTaskDto.additionalData();

        //set default maxPoints/status
        //BigDecimal maxPoints = modifyTaskDto.maxPoints() != null ? modifyTaskDto.maxPoints() : BigDecimal.TEN;
        //TaskStatus status = modifyTaskDto.status() != null ? modifyTaskDto.status() : TaskStatus.APPROVED;

        // Create task using factory
        ClusteringTask task = ClusteringTaskFactory.create(id,
            dto.numberOfClusters(),
            dto.distanceMetric(),
            dto.difficulty()
        );

        //this is needed for persistence
        for (Cluster cluster : task.getClusters()) {
            for (DataPoint dataPoint : cluster.getDataPoints()) {
                dataPoint.setCluster(cluster);
            }
        }

        return task;
    }

    @Override
    protected void updateTask(ClusteringTask task, ModifyTaskDto<ModifyClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        // Currently, update only supports resetting solution-related values
        // Optional: implement regeneration or restrictions here
        // Could add task.reset(); task.generateExercisePoints(); task.solve(); if needed
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updating clustering tasks is not yet supported.");
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(ClusteringTask task, boolean create) {
        String distanceMetric = task.getDistanceMetric().toString();
        String distance = this.messageSource.getMessage("distanceMetric." + distanceMetric, null, Locale.GERMAN);
        String formula = this.messageSource.getMessage("formula." + distanceMetric, null, Locale.GERMAN);
        String distanceEn = this.messageSource.getMessage("distanceMetric." + distanceMetric, null, Locale.ENGLISH);
        String formulaEn = this.messageSource.getMessage("formula." + distanceMetric, null, Locale.ENGLISH);
        String image = ClusteringImageGeneration.generateClusterImage(task, false);
        String pointsTableHtml = renderDataPointsTable(task.getExerciseClusters().stream()
            .flatMap(cluster -> cluster.getDataPoints().stream()).toList());

        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription",
                new Object[]{pointsTableHtml, task.getExerciseCentroidsAsString(), task.getNumberOfDataPoints(),
                    task.getNumberOfClusters(), distance, formula, image}, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskDescription",
                new Object[]{pointsTableHtml, task.getExerciseCentroidsAsString(), task.getNumberOfDataPoints(),
                    task.getNumberOfClusters(), distanceEn, formulaEn, image}, Locale.ENGLISH)
        );
    }
    public static String renderDataPointsTable(List<DataPoint> dataPoints) {
        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellpadding='5' cellspacing='0'>");
        html.append("<tr><th>Name</th><th>X</th><th>Y</th></tr>");

        for (DataPoint p : dataPoints.stream().sorted(Comparator.comparing(DataPoint::getName)).toList()) {
            html.append("<tr>")
                .append("<td>").append(p.getName()).append("</td>")
                .append("<td>").append(String.format(Locale.ROOT, "%.2f", p.getX())).append("</td>")
                .append("<td>").append(String.format(Locale.ROOT, "%.2f", p.getY())).append("</td>")
                .append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }
}
