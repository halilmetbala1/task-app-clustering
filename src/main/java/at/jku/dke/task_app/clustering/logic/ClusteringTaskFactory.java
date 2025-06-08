package at.jku.dke.task_app.clustering.logic;

import at.jku.dke.task_app.clustering.data.entities.*;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

/**
 * Factory class for creating fully initialized {@link ClusteringTask} instances.
 */
public class ClusteringTaskFactory {

    /**
     * Overload for minimal input (no group/status/points).
     */
    public static ClusteringTask create(Long id,
                                        int numberOfClusters,
                                        DistanceMetric distanceMetric,
                                        TaskLength taskLength,
                                        int deductionWrongClusters, int deductionWrongLabels, int deductionWrongCentroids,
                                        MessageSource messageSource) {

        ClusteringTask task = new ClusteringTask(id,
            numberOfClusters,
            distanceMetric,
            taskLength,
            deductionWrongClusters, deductionWrongLabels, deductionWrongCentroids
        );

        List<DataPoint> dataPointList = task.generateExercisePoints(); // generate raw points
        task.solve(dataPointList, messageSource);                  // run k-means and build exercise/solution clusters

        return task;
    }
}
