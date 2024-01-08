package at.jku.dke.task_app.binary_search.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask}
 *
 * @param solution The solution.
 */
public record BinarySearchTaskDto(@NotNull Integer solution) implements Serializable {
}
