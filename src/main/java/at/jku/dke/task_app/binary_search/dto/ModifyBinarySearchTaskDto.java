package at.jku.dke.task_app.binary_search.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a binary search task.
 *
 * @param solution The solution.
 */
public record ModifyBinarySearchTaskDto(@NotNull Integer solution) implements Serializable {
}
