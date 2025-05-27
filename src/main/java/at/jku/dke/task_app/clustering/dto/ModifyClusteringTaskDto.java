package at.jku.dke.task_app.clustering.dto;

import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents a data transfer object for modifying a clustering task.
 *
 * @param numberOfClusters   The number of clusters.
// * @param numberOfDataPoints The number of data points.
 * @param distanceMetric     The distance metric to use.
 * @param difficulty         The difficulty level.
 */
public record ModifyClusteringTaskDto(
    @NotNull @Min(1) Integer numberOfClusters,
    //@NotNull @Min(1) Integer numberOfDataPoints,
    @NotNull DistanceMetric distanceMetric,
    @NotNull DifficultyLevel difficulty
) implements Serializable {
}
