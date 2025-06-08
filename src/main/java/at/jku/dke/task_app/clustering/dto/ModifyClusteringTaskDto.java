package at.jku.dke.task_app.clustering.dto;

import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a clustering task.
 *
 * @param numberOfClusters              The number of clusters.
// * @param numberOfDataPoints          The number of data points.
 * @param distanceMetric                The distance metric to use.
 * @param taskLength                    The taskLength level.
 * @param deductionWrongClusters        Points deducted from wrong count of Clusters or Iterations per Cluster or Iteration.
 * @param deductionWrongLabels          Points deducted for wrong assignment of Data Point to Cluster inside an Iteration.
 * @param deductionWrongCentroids       Points deducted for wrong calculation of Centroid Coordinates.
 */
public record ModifyClusteringTaskDto(
    @NotNull @Min(1) Integer numberOfClusters,
    //@NotNull @Min(1) Integer numberOfDataPoints,
    @NotNull DistanceMetric distanceMetric,
    @NotNull TaskLength taskLength,
    @NotNull @Min(0) Integer deductionWrongClusters,
    @NotNull @Min(0) Integer deductionWrongLabels,
    @NotNull @Min(0) Integer deductionWrongCentroids

) implements Serializable {
}
