package at.jku.dke.task_app.clustering.logic;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.data.entities.*;
import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;

import java.math.BigDecimal;
import java.util.List;

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
                                        DifficultyLevel difficulty) {

        ClusteringTask task = new ClusteringTask(id,
            numberOfClusters,
            distanceMetric,
            difficulty
        );

        List<DataPoint> dataPointList = task.generateExercisePoints(); // generate raw points
        task.solve(dataPointList);                  // run k-means and build exercise/solution clusters

        return task;
    }
}
