package at.jku.dke.task_app.clustering.dto;

import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.entities.Cluster;

import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for returning clustering task preview or details.
 *
 * @param numberOfClusters   The number of clusters.
 * @param numberOfDataPoints The number of data points.
 * @param distanceMetric     The distance metric used.
 * @param difficulty         The difficulty level.
 */
public record ClusteringTaskDto(
    @NotNull @Min(1) Integer numberOfClusters,
    @NotNull @Min(1) Integer numberOfDataPoints,
    @NotNull DistanceMetric distanceMetric,
    @NotNull DifficultyLevel difficulty
) implements Serializable {

}
