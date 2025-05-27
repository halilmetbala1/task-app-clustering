package at.jku.dke.task_app.clustering.services;

import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.services.BaseSubmissionService;
import at.jku.dke.task_app.clustering.data.entities.ClusteringSubmission;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringSubmissionRepository;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import at.jku.dke.task_app.clustering.evaluation.EvaluationService;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link ClusteringSubmission}s.
 */
@Service
public class ClusteringSubmissionService extends BaseSubmissionService<ClusteringTask, ClusteringSubmission, ClusteringSubmissionDto> {

    private final EvaluationService evaluationService;

    /**
     * Creates a new instance of class {@link ClusteringSubmissionService}.
     *
     * @param submissionRepository The input repository.
     * @param taskRepository       The task repository.
     * @param evaluationService    The evaluation service.
     */
    public ClusteringSubmissionService(ClusteringSubmissionRepository submissionRepository, ClusteringTaskRepository taskRepository, EvaluationService evaluationService) {
        super(submissionRepository, taskRepository);
        this.evaluationService = evaluationService;
    }

    @Override
    protected ClusteringSubmission createSubmissionEntity(SubmitSubmissionDto<ClusteringSubmissionDto> submitSubmissionDto) {
        return new ClusteringSubmission(submitSubmissionDto.submission().input());
    }

    @Override
    protected GradingDto evaluate(SubmitSubmissionDto<ClusteringSubmissionDto> submitSubmissionDto) {
        return this.evaluationService.evaluate(submitSubmissionDto);
    }

    @Override
    protected ClusteringSubmissionDto mapSubmissionToSubmissionData(ClusteringSubmission submission) {
        return new ClusteringSubmissionDto(submission.getSubmission());
    }

}
