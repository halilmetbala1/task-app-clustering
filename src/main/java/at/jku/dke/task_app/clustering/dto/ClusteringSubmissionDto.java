package at.jku.dke.task_app.clustering.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * This class represents a data transfer object for submitting a solution.
 *
 * @param input The user input.
 */
public record ClusteringSubmissionDto(@NotNull @Size(max = 5000) String input) {
}
