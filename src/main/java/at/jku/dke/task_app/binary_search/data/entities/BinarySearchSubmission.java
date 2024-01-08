package at.jku.dke.task_app.binary_search.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseSubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a binary search input.
 */
@Entity
@Table(name = "submission")
public class BinarySearchSubmission extends BaseSubmission<BinarySearchTask> {
    @NotNull
    @Column(name = "submission", nullable = false)
    private String submission;

    /**
     * Creates a new instance of class {@link BinarySearchSubmission}.
     */
    public BinarySearchSubmission() {
    }

    /**
     * Creates a new instance of class {@link BinarySearchSubmission}.
     *
     * @param submission The input.
     */
    public BinarySearchSubmission(String submission) {
        this.submission = submission;
    }

    /**
     * Gets the input.
     *
     * @return The input.
     */
    public String getSubmission() {
        return submission;
    }

    /**
     * Sets the input.
     *
     * @param submission The input.
     */
    public void setSubmission(String submission) {
        this.submission = submission;
    }
}
