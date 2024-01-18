package at.jku.dke.task_app.binary_search.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskRepository;
import at.jku.dke.task_app.binary_search.dto.BinarySearchSubmissionDto;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service that evaluates submissions.
 */
@Service
public class EvaluationService {
    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    private final BinarySearchTaskRepository taskRepository;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link EvaluationService}.
     *
     * @param taskRepository The task repository.
     * @param messageSource  The message source.
     */
    public EvaluationService(BinarySearchTaskRepository taskRepository, MessageSource messageSource) {
        this.taskRepository = taskRepository;
        this.messageSource = messageSource;
    }

    /**
     * Evaluates a input.
     *
     * @param submission The input to evaluate.
     * @return The evaluation result.
     */
    @Transactional
    public GradingDto evaluate(SubmitSubmissionDto<BinarySearchSubmissionDto> submission) {
        // find task
        var task = this.taskRepository.findById(submission.taskId()).orElseThrow(() -> new EntityNotFoundException("Task " + submission.taskId() + " does not exist."));

        // evaluate input
        LOG.info("Evaluating input for task {} with mode {} and feedback-level {}", submission.taskId(), submission.mode(), submission.feedbackLevel());
        Locale locale = Locale.of(submission.language());
        BigDecimal points = BigDecimal.ZERO;
        List<CriterionDto> criteria = new ArrayList<>();
        String feedback;

        // parse input
        Integer input = null;
        NumberFormatException error = null;
        try {
            input = Integer.parseInt(submission.submission().input());
        } catch (NumberFormatException ex) {
            error = ex;
        }

        if (error != null) {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntax", null, locale),
                null,
                false,
                error.getMessage()));
        } else {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntax", null, locale),
                null,
                true,
                this.messageSource.getMessage("criterium.syntax.valid", null, locale)));
        }

        // evaluate and grade
        switch (submission.mode()) {
            case RUN:
                feedback = this.messageSource.getMessage("input", new Object[]{submission.submission().input()}, locale);
                break;
            case DIAGNOSE:
                feedback = this.messageSource.getMessage(error == null && input.equals(task.getSolution()) ? "correct" : "incorrect", null, locale);
                if (error == null) {
                    int diff = task.getSolution() - input;
                    if (diff == 0)
                        points = task.getMaxPoints();
                    if (submission.feedbackLevel() > 0)
                        criteria.add(new CriterionDto(
                            this.messageSource.getMessage("criterium.value", null, locale),
                            points,
                            diff == 0,
                            this.messageSource.getMessage(diff < 0 ? "criterium.value.less" : (diff > 0 ? "criterium.value.greater" : "criterium.value.equals"), null, locale)));
                }
                break;
            case SUBMIT:
                if (error == null && input.equals(task.getSolution())) {
                    feedback = this.messageSource.getMessage("correct", null, locale);
                    points = task.getMaxPoints();
                } else
                    feedback = this.messageSource.getMessage("incorrect", null, locale);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + submission.mode());
        }

        return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
    }
}
