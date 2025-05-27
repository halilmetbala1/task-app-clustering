package at.jku.dke.task_app.clustering.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Contains a minimum and a maximum number.
 *
 * @param min The minimum number.
 * @param max The maximum number.
 */
public record MinMaxDto(@NotNull int min, @NotNull int max) {
}
