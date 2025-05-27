package at.jku.dke.task_app.clustering.services;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.clustering.data.entities.ClusteringSubmission;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import at.jku.dke.task_app.clustering.evaluation.EvaluationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClusteringSubmissionServiceTest {

    @Test
    void createSubmissionEntity() {
        // Arrange
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new ClusteringSubmissionDto("33"));
        ClusteringSubmissionService service = new ClusteringSubmissionService(null, null, null);

        // Act
        ClusteringSubmission submission = service.createSubmissionEntity(dto);

        // Assert
        assertEquals(dto.submission().input(), submission.getSubmission());
    }

    @Test
    void mapSubmissionToSubmissionData() {
        // Arrange
        ClusteringSubmission submission = new ClusteringSubmission("33");
        ClusteringSubmissionService service = new ClusteringSubmissionService(null, null, null);

        // Act
        ClusteringSubmissionDto dto = service.mapSubmissionToSubmissionData(submission);

        // Assert
        assertEquals(submission.getSubmission(), dto.input());
    }

    @Test
    void evaluate() {
        // Arrange
        var evalService = mock(EvaluationService.class);
        SubmitSubmissionDto<ClusteringSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new ClusteringSubmissionDto("33"));
        ClusteringSubmissionService service = new ClusteringSubmissionService(null, null, evalService);

        // Act
        var result = service.evaluate(dto);

        // Assert
        verify(evalService).evaluate(dto);
    }

}
