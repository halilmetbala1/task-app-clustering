package at.jku.dke.task_app.clustering.dto;

import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for returning clustering task preview or details.
 *
 * @param numberOfClusters   The number of clusters.
 * @param numberOfDataPoints The number of data points.
 * @param distanceMetric     The distance metric used.
 * @param taskLength         The taskLength level.
 */
public record ClusteringTaskDto(
    @NotNull @Min(1) Integer numberOfClusters,
    @NotNull @Min(1) Integer numberOfDataPoints,
    @NotNull DistanceMetric distanceMetric,
    @NotNull TaskLength taskLength,
    @NotNull @Min(0) Integer deductionWrongClusters,
    @NotNull @Min(0) Integer deductionWrongLabels,
    @NotNull @Min(0) Integer deductionWrongCentroids,
    String solution
) implements Serializable {

}
