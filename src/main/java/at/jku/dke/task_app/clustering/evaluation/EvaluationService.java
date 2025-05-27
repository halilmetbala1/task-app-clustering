package at.jku.dke.task_app.clustering.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.clustering.config.AppConfig;
import at.jku.dke.task_app.clustering.data.entities.*;
import at.jku.dke.task_app.clustering.data.repositories.ClusteringTaskRepository;
import at.jku.dke.task_app.clustering.dto.ClusteringSubmissionDto;
import at.jku.dke.task_app.clustering.dto.PointDto;
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
        BigDecimal points = BigDecimal.ZERO;
        List<CriterionDto> criteria = new ArrayList<>();

        try {
            String feedback;
            List<IterationSnapshot> studentIterations = parseUserIterations(task, submission.submission().input());
            List<SolutionIteration> solutionIterations = task.getSolutionIterations();

            int matchedIterations = 0;
            int totalIterations = solutionIterations.size();
            int userIterations = studentIterations.size();
            int totalDataPoints = task.getNumberOfDataPoints();

            double totalScore = 0.0;
            // Point weighting: first and last iteration = 0.5, rest = evenly spread
            List<Double> iterationWeights = new ArrayList<>();
            double w = (totalIterations <= 1) ? 1.0 : 1.0 / (totalIterations - 1);

            for (int i = 0; i < totalIterations; i++) {
                double weight = (i == 0 || i == totalIterations - 1) && totalIterations > 1 ? w / 2 : w;
                iterationWeights.add(weight);
            }

            for (int i = 0; i < Math.min(userIterations, totalIterations); i++) {

                //String representation of solution iteration
                String expected = normalize(solutionIterations.get(i).getIterationString());
                //String representation of student iteration
                String actual = normalize(ClusteringTask.snapToString(studentIterations.get(i)));

                //Solution iteration
                IterationSnapshot expectedSnap = parseUserIterations(task, solutionIterations.get(i).getIterationString()).getFirst();
                //Student iteration
                IterationSnapshot actualSnap = studentIterations.get(i);


                // Variablen aus config laden
                double tolerance = AppConfig.getDouble("coordinate.tolerance", 0.5);

                //boolean match = expected.equals(actual);
                IterationComparison result = compareIteration(expectedSnap, actualSnap, tolerance);

                double iterationWeight = iterationWeights.get(i);

                if (result.centroidsMatch() && result.pointsMatch()) {
                    matchedIterations++;
                    totalScore += iterationWeight;
                }
                else if (submission.feedbackLevel() > 0) {
                    String fb = this.messageSource.getMessage("error.iteration", new Object[]{i + 1}, locale);
                    if (submission.feedbackLevel() >= 2) {

                        StringBuilder suggestion = new StringBuilder();

                        if(expectedSnap.centroids.size() != actualSnap.centroids.size()){
                            suggestion.append(
                                this.messageSource.getMessage(
                                    "hint.clusterCountError",
                                    new Object[]{expectedSnap.centroids.size(), actualSnap.centroids.size()}, locale)).append("\n");
                        }
                        else if (!result.centroidsMatch) {
                            suggestion.append(this.messageSource.getMessage("hint.centroidError", null, locale)).append("\n");
                        }
                        else if (!result.pointsMatch) {
                            suggestion.append(this.messageSource.getMessage("hint.assignmentError", null, locale)).append("\n");
                        }

                        fb += "\n" + suggestion.toString().trim();
                    }
                    if (submission.feedbackLevel() == 3) {
                        fb += this.messageSource.getMessage("error.expected", new Object[]{solutionIterations.get(i).getIterationString()}, locale);
                        fb += this.messageSource.getMessage("error.actual", new Object[]{studentIterations.get(i)}, locale);
                    }

                    criteria.add(new CriterionDto(
                        this.messageSource.getMessage("criterium.clustering.error", null, locale),
                        null,
                        false,
                        fb));
                }
            }

            if (userIterations < totalIterations && submission.feedbackLevel() > 0) {
                String msg = this.messageSource.getMessage("criterium.clustering.incomplete",
                new Object[]{userIterations, totalIterations}, locale);
                criteria.add(new CriterionDto(
                    this.messageSource.getMessage("criterium.clustering.error", null, locale),
                    null,
                    false,
                    msg
                ));
            }

            points = BigDecimal.valueOf(totalScore).multiply(task.getMaxPoints());

            feedback = this.messageSource.getMessage("evaluation.summary",
                new Object[]{matchedIterations, totalIterations}, locale);

            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.clustering.correct", null, locale),
                points,
                true,
                this.messageSource.getMessage("criterium.clustering.detail", new Object[]{matchedIterations, totalIterations}, locale)
            ));

            if (submission.feedbackLevel() > 0) {
                criteria.add(new CriterionDto(
                    this.messageSource.getMessage("criterium.maxPoints", null, locale),
                    task.getMaxPoints(),
                    true,
                    ""
                ));
                criteria.add(new CriterionDto(
                    this.messageSource.getMessage("criterium.achievedPoints", null, locale),
                    points,
                    true,
                    ""
                ));
            }

            if (submission.feedbackLevel() >= 2) {
                criteria.add(new CriterionDto(
                    this.messageSource.getMessage("criterium.visualization", null, locale),
                    null,
                    true,
                    ClusteringImageGeneration.generateClusterImage(task, true)
                ));
            }

            return new GradingDto(task.getMaxPoints(), points, feedback, criteria);

        } catch (IllegalArgumentException ex) {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntaxError", null, locale),
                null,
                false,
                ex.getMessage()
            ));
            return new GradingDto(task.getMaxPoints(), points,
                this.messageSource.getMessage("error.inputFormat", null, locale),
                criteria);
        }
    }
    record IterationComparison(boolean centroidsMatch, boolean pointsMatch) {}
    private IterationComparison compareIteration(IterationSnapshot expected, IterationSnapshot actual, double tolerance) {
        if (expected.centroids.size() != actual.centroids.size())
            return new IterationComparison(false, false);

        List<PointDto> unmatchedActualCentroids = new ArrayList<>(actual.centroids);
        boolean centroidsMatch = true;
        boolean pointsMatch = true;

        for (int i = 0; i < expected.centroids.size(); i++) {
            PointDto ec = expected.centroids.get(i);

            // Try to find a matching actual centroid
            Optional<PointDto> match = unmatchedActualCentroids.stream()
                .filter(ac -> Math.abs(ec.x() - ac.x()) <= tolerance && Math.abs(ec.y() - ac.y()) <= tolerance)
                .findFirst();

            if (match.isEmpty()) {
                centroidsMatch = false;
                continue;
            }

            PointDto matchedCentroid = match.get();
            int actualIndex = actual.centroids.indexOf(matchedCentroid);
            unmatchedActualCentroids.remove(matchedCentroid);

            List<DataPoint> expectedPoints = expected.assignments.get(i);
            List<DataPoint> actualPoints = actual.assignments.getOrDefault(actualIndex, List.of());

            Set<Character> expectedNames = expectedPoints.stream().map(DataPoint::getName).collect(Collectors.toSet());
            Set<Character> actualNames = actualPoints.stream().map(DataPoint::getName).collect(Collectors.toSet());

            if (!expectedNames.equals(actualNames)) {
                pointsMatch = false;
            }
        }

        return new IterationComparison(centroidsMatch, pointsMatch);
    }


    public static List<IterationSnapshot> parseUserIterations(ClusteringTask task, String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input is empty.");
        }

        Map<Character, DataPoint> pointMap = task.getExerciseClusters().stream()
            .flatMap(cluster -> cluster.getDataPoints().stream())
            .collect(Collectors.toMap(DataPoint::getName, p -> p));

        List<IterationSnapshot> parsedIterations = new ArrayList<>();
        String[] rawIterations = input.split("(?m)^--\\s*$"); // split on lines with only "--"

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
                            throw new IllegalArgumentException("Unknown point label: " + label);
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
                throw new IllegalArgumentException("Could not parse the following cluster(s):\n" +
                    String.join("\n", unmatchedClusters) +
                    "\nPlease check the format (e.g., (x,y): A, B)");
            }

            Set<Character> seen = new HashSet<>();
            Set<Character> duplicates = usedLabels.stream()
                .filter(label -> !seen.add(label))
                .collect(Collectors.toSet());

            if (!duplicates.isEmpty()) {
                throw new IllegalArgumentException("Duplicate labels in iteration: " + duplicates.stream().findFirst().get());
            }

            Set<Character> expectedLabels = pointMap.keySet();
            Set<Character> usedSet = new HashSet<>(usedLabels);
            Set<Character> missing = expectedLabels.stream()
                .filter(label -> !usedSet.contains(label))
                .collect(Collectors.toSet());

            if (!missing.isEmpty()) {
                throw new IllegalArgumentException("Missing labels in iteration: " + missing.stream().findFirst().get());
            }

            if (centroids.isEmpty() || assignments.isEmpty()) {
                throw new IllegalArgumentException("Invalid or empty iteration block:\n" + rawIteration);
            }

            parsedIterations.add(new IterationSnapshot(centroids, assignments));
        }

        return parsedIterations;
    }


    public record ParsedCluster(double centerX, double centerY, List<DataPoint> dataPoints) {}
    private static double parseNumber(String input) {
        try {
            return Double.parseDouble(input.replace(',', '.').trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: '" + input + "'");
        }
    }
    private static String normalize(String s) {
        return s.replaceAll("\\s+", "").replace(",", ".").toLowerCase();
    }
}
