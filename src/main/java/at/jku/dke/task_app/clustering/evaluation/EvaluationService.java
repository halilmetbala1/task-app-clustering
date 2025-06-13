package at.jku.dke.task_app.clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.clustering.config.AppConfig;
import at.jku.dke.task_app.clustering.data.entities.*;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import at.jku.dke.task_app.clustering.dto.PointDto;
import at.jku.dke.task_app.clustering.logic.ClusterSyntaxValidator;
import at.jku.dke.task_app.clustering.logic.ClusteringImageGeneration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that evaluates submissions.
 */
@Service
public class EvaluationService {
    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    private final ClusteringTaskRepository taskRepository;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link EvaluationService}.
     *
     * @param taskRepository The task repository.
     * @param messageSource  The message source.
     */
    public EvaluationService(ClusteringTaskRepository taskRepository, MessageSource messageSource) {
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
    public GradingDto evaluate(SubmitSubmissionDto<ClusteringSubmissionDto> submission) {
        ClusteringTask task = this.taskRepository.findById(submission.taskId())
            .orElseThrow(() -> new EntityNotFoundException("Task " + submission.taskId() + " does not exist."));

        LOG.info("Evaluating clustering input for task {} with mode {} and feedback-level {}",
            submission.taskId(), submission.mode(), submission.feedbackLevel());

        Locale locale = Locale.of(submission.language());
        List<CriterionDto> criteria = new ArrayList<>();
        BigDecimal totalScore = task.getMaxPoints();

        //Run always 0 points
        if(submission.mode() == SubmissionMode.RUN){
            totalScore = BigDecimal.ZERO;
        }

        try {

            String feedback = "";
            List<IterationSnapshot> studentIterations = parseUserIterations(task, submission.submission().input(), this.messageSource, locale);
            List<SolutionIteration> solutionIterations = task.getSolutionIterations();

            int matchedIterations = 0;
            int totalIterations = solutionIterations.size();
            int userIterations = studentIterations.size();
            int totalDataPoints = task.getNumberOfDataPoints();
            int deduction = 0;
            String suggestion = "";


            if (userIterations != totalIterations) {
                for(int j = 0; j<Math.abs(totalIterations - userIterations); j++){
                    deduction += task.getDeductionWrongClusters();
                    if(submission.mode() != SubmissionMode.RUN){
                        if(submission.feedbackLevel() > 0){
                            //"Wrong number of iterations have been submitted ({0}/{1})."
                            String msg = this.messageSource.getMessage("criterium.clustering.incomplete",
                                new Object[]{userIterations, totalIterations}, locale);
                            criteria.add(new CriterionDto(
                                //"Point deduction"
                                this.messageSource.getMessage("criterium.deduction.title", null, locale),
                                BigDecimal.valueOf(task.getDeductionWrongClusters()).negate(),
                                false,
                                msg
                            ));
                        }
                        if(submission.feedbackLevel() == 3){
                            suggestion =this.messageSource.getMessage(
                                //"Try finishing the optimization by recalculating the centroid positions."
                                "hint.iterationCountError",
                                new Object[]{userIterations, totalIterations}, locale);
                        }
                    }
                }
            }

            for (int i = 0; i < Math.min(userIterations, totalIterations); i++) {

                //Solution iteration
                IterationSnapshot expectedSnap = parseUserIterations(task, solutionIterations.get(i).getIterationString(), this.messageSource, locale).getFirst();
                //Student iteration
                IterationSnapshot actualSnap = studentIterations.get(i);

                //no feedback at RUN
                if(submission.mode() != SubmissionMode.RUN){

                    // Variablen aus config laden
                    double tolerance = AppConfig.getDouble("coordinate.tolerance", 0.5);

                    //boolean match = expected.equals(actual);
                    IterationComparison result = compareIteration(expectedSnap, actualSnap, tolerance);

                    String fb = "";
                    if (result.centroidsMatch() && result.pointsMatch()) {
                        matchedIterations++;
                        continue;
                    }
                    else{
                        //"Error in iteration: {0}."
                        fb = this.messageSource.getMessage("error.iteration", new Object[]{i + 1}, locale) + " ";

                        if (submission.feedbackLevel() == 3) {
                            //"Expected"
                            String error = this.messageSource.getMessage("error.expected", new Object[]{solutionIterations.get(i).getIterationString()}, locale);
                            //"Actual"
                            error += this.messageSource.getMessage("error.actual", new Object[]{studentIterations.get(i).toString()}, locale);
                            criteria.add(new CriterionDto(
                                //"Error"
                                this.messageSource.getMessage("criterium.clustering.error", null, locale),
                                null,
                                false,
                                error));
                        }
                    }

                    int clusterDiff = Math.abs(expectedSnap.centroids.size() - actualSnap.centroids.size());
                    if(clusterDiff > 0){

                        for(int j = 0; j<clusterDiff; j++){
                            deduction += task.getDeductionWrongClusters();

                            if (submission.feedbackLevel() > 0) {
                                criteria.add(new CriterionDto(
                                    //"Point deduction"
                                    this.messageSource.getMessage("criterium.deduction.title", null, locale),
                                    //-2
                                    BigDecimal.valueOf(task.getDeductionWrongClusters()).negate(),
                                    false,
                                    //"Wrong amount of clusters."
                                    fb + this.messageSource.getMessage("criterium.deduction.wrongClusters", null, locale)));
                            }
                        }
                        if(submission.feedbackLevel() >= 2){
                            suggestion = this.messageSource.getMessage(
                                    //"Number of clusters provided incorrect. Should be: {0}. Is: {1}."
                                    "hint.clusterCountError",
                                    new Object[]{expectedSnap.centroids.size(), actualSnap.centroids.size()}, locale);
                        }
                    }
                    if (!result.centroidsMatch) {

                        for(int j = 0; j<result.centroidErrors();j++){
                            deduction += task.getDeductionWrongCentroids();

                            if (submission.feedbackLevel() > 0) {
                                criteria.add(new CriterionDto(
                                    //"Point deduction"
                                    this.messageSource.getMessage("criterium.deduction.title", null, locale),
                                    //-1
                                    BigDecimal.valueOf(task.getDeductionWrongCentroids()).negate(),
                                    false,
                                    //"Wrong coordinate of centroid."
                                    fb + this.messageSource.getMessage("criterium.deduction.wrongCentroids", null, locale)));
                            }

                        }
                        if(submission.feedbackLevel() >= 2) {
                            //"Centroid coordinates incorrect. Maybe try recalculating the centroid position?"
                            suggestion = this.messageSource.getMessage("hint.centroidError", null, locale);
                        }
                    }
                    if (!result.pointsMatch) {

                        for(int j = 0; j<result.pointErrors(); j++){

                            deduction += task.getDeductionWrongLabels();

                            if (submission.feedbackLevel() > 0) {
                                criteria.add(new CriterionDto(
                                    //"Point deduction"
                                    this.messageSource.getMessage("criterium.deduction.title", null, locale),
                                    //-1
                                    BigDecimal.valueOf(task.getDeductionWrongLabels()).negate(),
                                    false,
                                    //"Wrong assignment of data point."
                                    fb + this.messageSource.getMessage("criterium.deduction.wrongAssignment", null, locale)));
                            }
                        }
                        if(submission.feedbackLevel() >= 2) {
                            //"Data point assigned incorrectly. Maybe try recalculating the distances?"
                            suggestion = this.messageSource.getMessage("hint.assignmentError", null, locale);
                        }
                    }
                }
            }

            if(submission.mode() == SubmissionMode.RUN || submission.feedbackLevel() == 0){
                //"No syntax errors."
                feedback = this.messageSource.getMessage("feedback.correctSyntax",
                    null, locale);
            }
            if(submission.mode() != SubmissionMode.RUN){

                if (submission.feedbackLevel() > 0) {
                    //"{0} of {1} iterations were evaluated correctly."
                    feedback = this.messageSource.getMessage("evaluation.summary",
                        new Object[]{matchedIterations, totalIterations}, locale);
                }

                //Give the user specific hints in full feedback mode
                if (submission.feedbackLevel() == 3 && !suggestion.isEmpty()) {
                    criteria.add(new CriterionDto(
                        //"Hint"
                        this.messageSource.getMessage("criterium.clustering.hint", null, locale),
                        null,
                        false,
                        suggestion));
                }

                if (submission.feedbackLevel() >= 2) {
                    //"Visualization"
                    criteria.add(new CriterionDto(
                        this.messageSource.getMessage("criterium.visualization", null, locale),
                        null,
                        true,
                        ClusteringImageGeneration.generateClusterImage(task, true, messageSource, locale)
                    ));
                }

                if (submission.feedbackLevel() == 3) {
                    String fb = task.getSolutionIterationsString();

                    //"Solution"
                    criteria.add(new CriterionDto(
                        this.messageSource.getMessage("criterium.clustering.solution", null, locale),
                        null,
                        true,
                        fb));

                    fb = task.getSolutionInstructions(locale);

                    //"Instructions"
                    criteria.add(new CriterionDto(
                        this.messageSource.getMessage("criterium.clustering.instructions", null, locale),
                        null,
                        true,
                        fb));
                }
            }

            //deduct points
            totalScore = totalScore.subtract(BigDecimal.valueOf(deduction));
            //at least 0 points, no negative points
            totalScore = totalScore.max(BigDecimal.ZERO);

            return new GradingDto(task.getMaxPoints(), totalScore, feedback, criteria);

        } catch (IllegalArgumentException ex) {
            //Syntax Error: automatic 0 points
            totalScore = BigDecimal.ZERO;

            criteria.add(new CriterionDto(
                //"Syntax Error"
                this.messageSource.getMessage("criterium.syntaxError", null, locale),
                task.getMaxPoints().negate(),
                false,
                ex.getMessage()
            ));
            return new GradingDto(task.getMaxPoints(), totalScore,
                //"Invalid input format."
                this.messageSource.getMessage("error.inputFormat", null, locale),
                criteria);
        }
    }
    record IterationComparison(boolean centroidsMatch, boolean pointsMatch, int centroidErrors, int pointErrors) {}
    private IterationComparison compareIteration(IterationSnapshot expected, IterationSnapshot actual, double tolerance) {
        if (expected.centroids.size() != actual.centroids.size())
            return new IterationComparison(false, false, Math.abs(expected.centroids.size() - actual.centroids.size()), 0);

        List<PointDto> unmatchedActualCentroids = new ArrayList<>(actual.centroids);
        boolean centroidsMatch = true;

        int centroidErrorCount = 0;

        Map<Character, Integer> expectedPointMap = new HashMap<>();
        Map<Character, Integer> actualPointMap = new HashMap<>();

        for (int i = 0; i < expected.centroids.size(); i++) {
            PointDto ec = expected.centroids.get(i);

            // Try to find a matching actual centroid
            Optional<PointDto> match = unmatchedActualCentroids.stream()
                .filter(ac -> Math.abs(ec.x() - ac.x()) <= tolerance && Math.abs(ec.y() - ac.y()) <= tolerance)
                .findFirst();

            if (match.isEmpty()) {
                centroidsMatch = false;
                centroidErrorCount++;
                continue;
            }

            PointDto matchedCentroid = match.get();
            int actualIndex = actual.centroids.indexOf(matchedCentroid);
            unmatchedActualCentroids.remove(matchedCentroid);

            List<DataPoint> expectedPoints = expected.assignments.get(i);
            List<DataPoint> actualPoints = actual.assignments.getOrDefault(actualIndex, List.of());

            for (DataPoint dp : expectedPoints) {
                expectedPointMap.put(dp.getName(), i);
            }
            for (DataPoint dp : actualPoints) {
                actualPointMap.put(dp.getName(), actualIndex);
            }
        }

        // Calculate point mismatches only once
        Set<Character> allPointNames = new HashSet<>();
        allPointNames.addAll(expectedPointMap.keySet());
        allPointNames.addAll(actualPointMap.keySet());

        int pointErrorCount = 0;
        for (Character name : allPointNames) {
            Integer expectedCluster = expectedPointMap.get(name);
            Integer actualCluster = actualPointMap.get(name);
            if (!Objects.equals(expectedCluster, actualCluster)) {
                pointErrorCount++;
            }
        }

        boolean pointsMatch = pointErrorCount == 0;

        return new IterationComparison(centroidsMatch, pointsMatch, centroidErrorCount, pointErrorCount);
    }

    public static void validateFullSyntax(String input, MessageSource messageSource, Locale locale) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                messageSource.getMessage("error.inputEmpty", null, locale)
            );
        }

        String cleaned = input.replaceAll("\\s+", "");
        String[] iterations = cleaned.split("--");

        int offset = 0;
        for (String iteration : iterations) {
            // Check for stray single or triple dashes in this iteration
            int strayDashIndex = iteration.indexOf("-");
            if (strayDashIndex != -1) {
                int absoluteIndex = offset + strayDashIndex;
                int errorStart = Math.max(0, absoluteIndex - 10);
                int errorEnd = Math.min(cleaned.length(), absoluteIndex + 10);
                String errorContext = cleaned.substring(errorStart, errorEnd);

                throw new IllegalArgumentException(
                    messageSource.getMessage("clustering.input.syntaxErrorAt", new Object[]{errorContext}, locale)
                );
            }

            //Count iteration matches inside iteration.
            //This way, we know if there was a faulty separator
            Matcher matcher = ClusterSyntaxValidator.clusterGroupPattern.matcher(iteration);
            int matchCount = 0;
            while (matcher.find()) matchCount++;

            if (!ClusterSyntaxValidator.isValidClusterGroup(iteration) || matchCount == 0) {
                int index = ClusterSyntaxValidator.indexOfLastMatch(cleaned);
                String errorContext = cleaned.substring(Math.max(0, index - 10), Math.min(cleaned.length(), index + 10));
                throw new IllegalArgumentException(
                    messageSource.getMessage("clustering.input.syntaxErrorAt", new Object[]{errorContext}, locale)
                );
            }

            //detect likely separator mistake if match count is too high
            if (matchCount > 2) {
                int index = cleaned.indexOf(iteration);
                String errorContext = cleaned.substring(Math.max(0, index - 10), Math.min(cleaned.length(), index + 10));
                throw new IllegalArgumentException(
                    messageSource.getMessage("clustering.input.faultySeparator", new Object[]{errorContext}, locale)
                );
            }
            // Move offset past this iteration and the "--" separator
            offset += iteration.length() + 2; // +2 for "--"
        }
    }

    public static List<IterationSnapshot> parseUserIterations(ClusteringTask task, String input,MessageSource messageSource, Locale locale) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("error.inputEmpty", null, locale));
        }

        //Clean student submission of white spaces
        input = input.replaceAll("\\s+", "");

        //Validate full syntax before parsing, throws exceptions
        validateFullSyntax(input, messageSource, locale);

        Map<Character, DataPoint> pointMap = task.getExerciseClusters().stream()
            .flatMap(cluster -> cluster.getDataPoints().stream())
            .collect(Collectors.toMap(DataPoint::getName, p -> p));

        List<IterationSnapshot> parsedIterations = new ArrayList<>();
        String[] rawIterations = input.split("--"); // split on lines with only "--"

        for (String rawIteration : rawIterations) {

            List<PointDto> centroids = new ArrayList<>();
            Map<Integer, List<DataPoint>> assignments = new HashMap<>();
            List<Character> usedLabels = new ArrayList<>();
            List<String> unmatchedClusters = new ArrayList<>();
            int clusterIndex = 0;

            String[] rawClusters = rawIteration.split("(?<=\\])\\s*;\\s*"); // split on clusters ending with ]

            Pattern pattern = Pattern.compile("\\(([^,]+),([^\\)]+)\\):\\s*([A-Z](?:\\s*,\\s*[A-Z])*)");

            for (String rawCluster : rawClusters) {
                Matcher matcher = pattern.matcher(rawCluster.trim());

                if (matcher.find()) {
                    double x = parseNumber(matcher.group(1));
                    double y = parseNumber(matcher.group(2));
                    centroids.add(new PointDto(x, y));

                    String[] labels = matcher.group(3).split("\\s*,\\s*");

                    List<DataPoint> points = new ArrayList<>();
                    for (String label : labels) {
                        if (label.length() != 1 || !pointMap.containsKey(label.charAt(0))) {
                            throw new IllegalArgumentException(
                                messageSource.getMessage("error.labelUnknown", new Object[]{label}, locale));
                        }
                        points.add(pointMap.get(label.charAt(0)));
                        usedLabels.add(label.charAt(0));
                    }

                    assignments.put(clusterIndex++, points);
                } else {
                    unmatchedClusters.add(rawCluster.trim());
                }
            }

            if (!unmatchedClusters.isEmpty()) {
                throw new IllegalArgumentException(
                    messageSource.getMessage("error.unmatchedClusters", new Object[]{
                        String.join("\n", unmatchedClusters)
                    }, locale)
                );
            }

            Set<Character> seen = new HashSet<>();
            Set<Character> duplicates = usedLabels.stream()
                .filter(label -> !seen.add(label))
                .collect(Collectors.toSet());

            if (!duplicates.isEmpty()) {
                throw new IllegalArgumentException(
                    messageSource.getMessage("error.duplicateLabels", new Object[]{duplicates.stream().findFirst().get()}, locale));
            }

            Set<Character> expectedLabels = pointMap.keySet();
            Set<Character> usedSet = new HashSet<>(usedLabels);
            Set<Character> missing = expectedLabels.stream()
                .filter(label -> !usedSet.contains(label))
                .collect(Collectors.toSet());

            if (!missing.isEmpty()) {
                throw new IllegalArgumentException(
                    messageSource.getMessage("error.missingLabels", new Object[]{missing.stream().findFirst().get()}, locale));
            }

            if (centroids.isEmpty() || assignments.isEmpty()) {
                throw new IllegalArgumentException(
                    messageSource.getMessage("error.emptyIteration", new Object[]{rawIteration}, locale));
            }

            parsedIterations.add(new IterationSnapshot(centroids, assignments));
        }

        return parsedIterations;
    }

    private static double parseNumber(String input) {
        try {
            return Double.parseDouble(input.replace(',', '.').trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: '" + input + "'");
        }
    }
}
