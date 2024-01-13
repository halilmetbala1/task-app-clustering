package at.jku.dke.task_app.binary_search.dto;

import at.jku.dke.task_app.binary_search.validation.ValidTaskGroupNumber;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a binary search task group.
 *
 * @param minNumber The minimum number.
 * @param maxNumber The maximum number.
 */
@ValidTaskGroupNumber
public record ModifyBinarySearchTaskGroupDto(@NotNull Integer minNumber, @NotNull Integer maxNumber) implements Serializable {
}
