package at.jku.dke.task_app.binary_search.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseSubmission;
import at.jku.dke.etutor.task_app.dto.SubmissionMode;
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
     * Creates a new instance of class {@link BinarySearchSubmission}.
     *
     * @param userId        The user id.
     * @param assignmentId  The assignment id.
     * @param task          The task.
     * @param language      The language.
     * @param feedbackLevel The feedback level.
     * @param mode          The mode.
     * @param submission    The input.
     */
    public BinarySearchSubmission(String userId, String assignmentId, BinarySearchTask task, String language, int feedbackLevel, SubmissionMode mode, String submission) {
        super(userId, assignmentId, task, language, feedbackLevel, mode);
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
