package at.jku.dke.task_app.clustering.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseSubmissionController;
import at.jku.dke.task_app.clustering.data.entities.ClusteringSubmission;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import at.jku.dke.task_app.clustering.services.ClusteringSubmissionService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link ClusteringSubmission}s.
 */
@RestController
public class SubmissionController extends BaseSubmissionController<ClusteringSubmissionDto> {
    /**
     * Creates a new instance of class {@link SubmissionController}.
     *
     * @param submissionService The input service.
     */
    public SubmissionController(ClusteringSubmissionService submissionService) {
        super(submissionService);
    }
}
