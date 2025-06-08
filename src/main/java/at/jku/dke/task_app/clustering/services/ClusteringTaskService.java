package at.jku.dke.task_app.clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.clustering.data.entities.*;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ModifyClusteringTaskDto;
import at.jku.dke.task_app.clustering.logic.ClusteringImageGeneration;
import at.jku.dke.task_app.clustering.logic.ClusteringTaskFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            dto.taskLength(),
            dto.deductionWrongClusters(),
            dto.deductionWrongLabels(),
            dto.deductionWrongCentroids(),
            messageSource
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
        if (!modifyTaskDto.taskType().equals("clustering")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");
        }
        ModifyClusteringTaskDto dto = modifyTaskDto.additionalData();

        TaskLength oldTaskLength = task.getTaskLength();
        DistanceMetric oldDistance = task.getDistanceMetric();
        int oldClusterCount = task.getNumberOfClusters();

        TaskLength newTaskLength = dto.taskLength();
        DistanceMetric newDistance = dto.distanceMetric();
        int newClusterCount = dto.numberOfClusters();

        task.setTaskLength(newTaskLength);
        task.setDistanceMetric(newDistance);
        task.setNumberOfClusters(newClusterCount);

        task.setDeductionWrongClusters(dto.deductionWrongClusters());
        task.setDeductionWrongLabels(dto.deductionWrongLabels());
        task.setDeductionWrongCentroids(dto.deductionWrongCentroids());

        boolean needsNewPoints = !oldTaskLength.equals(newTaskLength);
        boolean needsNewSolution = needsNewPoints || !oldDistance.equals(newDistance) || oldClusterCount != newClusterCount;

        if (needsNewPoints) {
            List<DataPoint> newPoints = task.generateExercisePoints();
            task.solve(newPoints, messageSource);
        } else if (needsNewSolution) {
            List<DataPoint> existingPoints = task.getExerciseClusters().stream()
                .flatMap(cluster -> cluster.getDataPoints().stream()).toList();
            task.solve(existingPoints, messageSource);
        }

        repository.save(task);
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(ClusteringTask task, boolean create) {
        String distanceMetric = task.getDistanceMetric().toString();
        String distance = this.messageSource.getMessage("distanceMetric." + distanceMetric, null, Locale.GERMAN);
        String formula = this.messageSource.getMessage("formula." + distanceMetric, null, Locale.GERMAN);
        String distanceEn = this.messageSource.getMessage("distanceMetric." + distanceMetric, null, Locale.ENGLISH);
        String formulaEn = this.messageSource.getMessage("formula." + distanceMetric, null, Locale.ENGLISH);
        String image = ClusteringImageGeneration.generateClusterImage(task, false, messageSource, Locale.GERMAN);
        String imageEN = ClusteringImageGeneration.generateClusterImage(task, false, messageSource, Locale.ENGLISH);
        String pointsTableHtml = DataPoint.renderDataPointsTable(task.getExerciseClusters().stream()
            .flatMap(cluster -> cluster.getDataPoints().stream()).toList());

        String messageGER = this.messageSource.getMessage("defaultTaskDescription",
            new Object[]{pointsTableHtml, task.getExerciseCentroidsAsString(), task.getNumberOfDataPoints(),
                task.getNumberOfClusters(), distance, formula, image}, Locale.GERMAN);
        String messageENG = this.messageSource.getMessage("defaultTaskDescription",
            new Object[]{pointsTableHtml, task.getExerciseCentroidsAsString(), task.getNumberOfDataPoints(),
                task.getNumberOfClusters(), distanceEn, formulaEn, imageEN}, Locale.ENGLISH);

        return new TaskModificationResponseDto(
            messageGER,
            messageENG
        );
    }
}
