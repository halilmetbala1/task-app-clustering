package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchSubmission;
import at.jku.dke.task_app.binary_search.dto.BinarySearchSubmissionDto;
import at.jku.dke.task_app.binary_search.evaluation.EvaluationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BinarySearchSubmissionServiceTest {

    @Test
    void createSubmissionEntity() {
        // Arrange
        SubmitSubmissionDto<BinarySearchSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new BinarySearchSubmissionDto("33"));
        BinarySearchSubmissionService service = new BinarySearchSubmissionService(null, null, null);

        // Act
        BinarySearchSubmission submission = service.createSubmissionEntity(dto);

        // Assert
        assertEquals(dto.submission().input(), submission.getSubmission());
    }

    @Test
    void mapSubmissionToSubmissionData() {
        // Arrange
        BinarySearchSubmission submission = new BinarySearchSubmission("33");
        BinarySearchSubmissionService service = new BinarySearchSubmissionService(null, null, null);

        // Act
        BinarySearchSubmissionDto dto = service.mapSubmissionToSubmissionData(submission);

        // Assert
        assertEquals(submission.getSubmission(), dto.input());
    }

    @Test
    void evaluate() {
        // Arrange
        var evalService = mock(EvaluationService.class);
        SubmitSubmissionDto<BinarySearchSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new BinarySearchSubmissionDto("33"));
        BinarySearchSubmissionService service = new BinarySearchSubmissionService(null, null, evalService);

        // Act
        var result = service.evaluate(dto);

        // Assert
        verify(evalService).evaluate(dto);
    }

}
