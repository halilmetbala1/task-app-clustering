package at.jku.dke.task_app.clustering.data.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClusteringSubmissionTest {

    @Test
    void testConstructor() {
        // Arrange
        var expected = "test";

        // Act
        var submission = new ClusteringSubmission(expected);
        var actual = submission.getSubmission();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testGetSetSubmission() {
        // Arrange
        var submission = new ClusteringSubmission();
        var expected = "test";

        // Act
        submission.setSubmission(expected);
        var actual = submission.getSubmission();

        // Assert
        assertEquals(expected, actual);
    }

}
