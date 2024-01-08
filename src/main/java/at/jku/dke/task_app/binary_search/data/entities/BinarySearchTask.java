package at.jku.dke.task_app.binary_search.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTask;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Represents a binary search task.
 */
@Entity
@Table(name = "task")
public class BinarySearchTask extends BaseTask<BinarySearchTaskGroup> {
    @NotNull
    @Column(name = "solution", nullable = false)
    private Integer solution;

    /**
     * Creates a new instance of class {@link BinarySearchTask}.
     */
    public BinarySearchTask() {
    }

    /**
     * Creates a new instance of class {@link BinarySearchTask}.
     *
     * @param solution The solution.
     */
    public BinarySearchTask(Integer solution) {
        this.solution = solution;
    }

    /**
     * Creates a new instance of class {@link BinarySearchTask}.
     *
     * @param maxPoints The maximum points.
     * @param status    The status.
     * @param taskGroup The task group.
     * @param solution  The solution.
     */
    public BinarySearchTask(BigDecimal maxPoints, TaskStatus status, BinarySearchTaskGroup taskGroup, Integer solution) {
        super(maxPoints, status, taskGroup);
        this.solution = solution;
    }

    /**
     * Creates a new instance of class {@link BinarySearchTask}.
     *
     * @param id        The identifier.
     * @param maxPoints The maximum points.
     * @param status    The status.
     * @param taskGroup The task group.
     * @param solution  The solution.
     */
    public BinarySearchTask(Long id, BigDecimal maxPoints, TaskStatus status, BinarySearchTaskGroup taskGroup, Integer solution) {
        super(id, maxPoints, status, taskGroup);
        this.solution = solution;
    }

    /**
     * Gets the solution.
     *
     * @return The solution.
     */
    public Integer getSolution() {
        return solution;
    }

    /**
     * Sets the solution.
     *
     * @param solution The solution.
     */
    public void setSolution(Integer solution) {
        this.solution = solution;
    }
}
