package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTask;
import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.config.AppConfig;
import at.jku.dke.task_app.clustering.data.entities.enums.ClusterType;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.entities.enums.TaskLength;
import at.jku.dke.task_app.clustering.dto.PointDto;
import at.jku.dke.task_app.clustering.logic.ClusteringImageGeneration;
import at.jku.dke.task_app.clustering.services.ClusteringMessageService;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "task")
public class ClusteringTask extends BaseTask {

    @NotNull
    @Column(name = "num_clusters", nullable = false)
    private int numberOfClusters;

    @NotNull
    @Column(name = "num_data_points", nullable = false)
    private int numberOfDataPoints;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "distance_metric", nullable = false)
    private DistanceMetric distanceMetric;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "taskLength", nullable = false)
    private TaskLength taskLength;

    @Column(columnDefinition = "TEXT")
    private String solutionInstructionsDe;

    @Column(columnDefinition = "TEXT")
    private String solutionInstructionsEn;

    @NotNull
    @Column(name = "deduction_wrong_clusters", nullable = false)
    private int deductionWrongClusters;

    @NotNull
    @Column(name = "deduction_wrong_labels", nullable = false)
    private int deductionWrongLabels;

    @NotNull
    @Column(name = "deduction_wrong_centroids", nullable = false)
    private int deductionWrongCentroids;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Cluster> clusters = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderId ASC")
    private List<SolutionIteration> solutionIterations = new ArrayList<>();
    public ClusteringTask() {
        this.solutionInstructionsDe = "";
        this.solutionInstructionsEn = "";
        this.distanceMetric = DistanceMetric.EUCLIDEAN;
    }

    public ClusteringTask(Long id, int numberOfClusters,
                          DistanceMetric distanceMetric, TaskLength taskLength,
                          int deductionWrongClusters, int deductionWrongLabels, int deductionWrongCentroids) {

        this.setId(id);
        this.numberOfClusters = numberOfClusters;
        this.numberOfDataPoints = 0;
        this.distanceMetric = distanceMetric;
        this.taskLength = taskLength;
        this.deductionWrongClusters = deductionWrongClusters;
        this.deductionWrongLabels = deductionWrongLabels;
        this.deductionWrongCentroids = deductionWrongCentroids;
        this.solutionInstructionsDe = "";
        this.solutionInstructionsEn = "";
    }

    public ClusteringTask(BigDecimal maxPoints, TaskStatus status,
                          int numberOfClusters, DistanceMetric distanceMetric, TaskLength taskLength) {
        super(maxPoints, status);

        this.numberOfClusters = numberOfClusters;
        this.numberOfDataPoints = 0;
        this.distanceMetric = distanceMetric;
        this.taskLength = taskLength;
        this.solutionInstructionsDe = "";
        this.solutionInstructionsEn = "";
    }

    public ClusteringTask(Long id, BigDecimal maxPoints, TaskStatus status,
                          int numberOfClusters, DistanceMetric distanceMetric, TaskLength taskLength,
                          int deductionWrongClusters, int deductionWrongLabels, int deductionWrongCentroids) {
        super(id, maxPoints, status);

        this.numberOfClusters = numberOfClusters;
        this.numberOfDataPoints = 0;
        this.distanceMetric = distanceMetric;
        this.taskLength = taskLength;
        this.deductionWrongClusters = deductionWrongClusters;
        this.deductionWrongLabels = deductionWrongLabels;
        this.deductionWrongCentroids = deductionWrongCentroids;
        this.solutionInstructionsDe = "";
        this.solutionInstructionsEn = "";
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public List<Cluster> getExerciseClusters() {
        return clusters.stream()
            .filter(c -> c.getType() == ClusterType.EXERCISE)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Cluster> getSolutionClusters() {
        return clusters.stream()
            .filter(c -> c.getType() == ClusterType.SOLUTION)
            .collect(Collectors.toCollection(ArrayList::new));
    }
    public String getExerciseDataPointsAsString() {
        return getExerciseClusters().stream()
            .flatMap(cluster -> cluster.getDataPoints().stream())
            .sorted(Comparator.comparing(DataPoint::getName)) // sort by name
            .map(p -> p.getName() + "(" + format(p.getX()) + ";" + format(p.getY()) + ")")
            .collect(Collectors.joining("; "));
    }

    private static final double EPSILON = 0.0001;

    public String getExerciseCentroidsAsString() {
        return getExerciseClusters().stream()
            .map(c -> {
                DataPoint match = c.getDataPoints().stream()
                    .filter(dp -> Math.abs(dp.getX() - c.getX()) < EPSILON &&
                        Math.abs(dp.getY() - c.getY()) < EPSILON)
                    .findFirst()
                    .orElse(null);
                return c.getName() + ": " + (match != null ? match.getName() : "?");
            })
            .collect(Collectors.joining("; "));
    }
    private String format(double value) {
        return String.format(Locale.GERMANY, "%.2f", value); // uses , as decimal separator
    }
    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public void setNumberOfClusters(int numberOfClusters) {
        this.numberOfClusters = numberOfClusters;
    }

    public int getNumberOfDataPoints() {
        return numberOfDataPoints;
    }

    public void setNumberOfDataPoints(int numberOfDataPoints) {
        this.numberOfDataPoints = numberOfDataPoints;
    }

    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    public void setDistanceMetric(DistanceMetric distanceMetric) {
        this.distanceMetric = distanceMetric;
    }

    public TaskLength getTaskLength() {
        return taskLength;
    }

    public void setTaskLength(TaskLength taskLength) {
        this.taskLength = taskLength;
    }

    public List<SolutionIteration> getSolutionIterations() {
        return solutionIterations;
    }

    public void setSolutionIterations(List<SolutionIteration> solutionIterations) {
        this.solutionIterations = solutionIterations;
    }

    public int getDeductionWrongClusters() {
        return deductionWrongClusters;
    }

    public void setDeductionWrongClusters(int deductionWrongClusters) {
        this.deductionWrongClusters = deductionWrongClusters;
    }

    public int getDeductionWrongLabels() {
        return deductionWrongLabels;
    }

    public void setDeductionWrongLabels(int deductionWrongLabels) {
        this.deductionWrongLabels = deductionWrongLabels;
    }

    public int getDeductionWrongCentroids() {
        return deductionWrongCentroids;
    }

    public void setDeductionWrongCentroids(int deductionWrongCentroids) {
        this.deductionWrongCentroids = deductionWrongCentroids;
    }

    public String getSolutionInstructionsDe() {
        return solutionInstructionsDe;
    }

    public void setSolutionInstructionsDe(String solutionInstructionsDe) {
        this.solutionInstructionsDe = solutionInstructionsDe;
    }

    public String getSolutionInstructionsEn() {
        return solutionInstructionsEn;
    }

    public void setSolutionInstructionsEn(String solutionInstructionsEn) {
        this.solutionInstructionsEn = solutionInstructionsEn;
    }

    public List<DataPoint> generateExercisePoints() {
        // Load variables from config
        int lowerBound = AppConfig.getInt("numberOfDataPoints.taskLength." + taskLength.toString() + ".lowerBound", 8);
        int upperBound = AppConfig.getInt("numberOfDataPoints.taskLength." + taskLength.toString() + ".upperBound", 12);

        Random random = new Random();
        int pointCount = lowerBound + random.nextInt(upperBound - lowerBound + 1); // inclusive upperBound

        this.numberOfDataPoints = pointCount;

        List<DataPoint> points = new ArrayList<>();
        for (int i = 0; i < pointCount; i++) {
            int x = random.nextInt(101); // 0–100 inclusive
            int y = random.nextInt(101);

            points.add(new DataPoint(x, y, (char)(((int)'A') + i), null));
        }

        return points;
    }

    public void solve(List<DataPoint> inputPoints, MessageSource messageSource) {
        final int MAX_RETRIES = 100;
        int retries = 0;

        while (retries < MAX_RETRIES) {
            retries++;

            //Clean up existing clusters and iterations
            this.getClusters().clear();
            this.solutionIterations.clear();

            if (inputPoints.isEmpty() || numberOfClusters <= 0)
                throw new IllegalStateException("Invalid task setup for solving.");

            //choose cluster centroids based on taskLength
            List<PointDto> centroids = this.assignCentroidsByTaskLength(inputPoints);

            List<IterationSnapshot> snapshots = new ArrayList<>();
            boolean changed = false;

            List<Integer> previousAssignments = new ArrayList<>(Collections.nCopies(inputPoints.size(), -1));

            StringBuilder htmlDocDe = new StringBuilder();
            StringBuilder htmlDocEn = new StringBuilder();

            htmlDocDe.append("<h3>").append(messageSource.getMessage("doc.steps.title", null, Locale.GERMAN)).append("</h3><ol>");
            htmlDocEn.append("<h3>").append(messageSource.getMessage("doc.steps.title", null, Locale.ENGLISH)).append("</h3><ol>");

            htmlDocDe.append("<li>").append(messageSource.getMessage("doc.steps.initCenters", null, Locale.GERMAN)).append("<ul>");
            htmlDocEn.append("<li>").append(messageSource.getMessage("doc.steps.initCenters", null, Locale.ENGLISH)).append("<ul>");
            for (int i = 0; i < centroids.size(); i++) {
                PointDto c = centroids.get(i);
                htmlDocDe.append(messageSource.getMessage("doc.steps.initCenter.line", new Object[]{i + 1, c.x(), c.y()}, Locale.GERMAN));
                htmlDocEn.append(messageSource.getMessage("doc.steps.initCenter.line", new Object[]{ i + 1, c.x(), c.y()}, Locale.ENGLISH));
            }
            htmlDocDe.append("</ul></li>");
            htmlDocEn.append("</ul></li>");

            for (int iteration = 0; iteration < 100; iteration++) {
                Map<Integer, List<DataPoint>> assignments = new HashMap<>();
                for (int i = 0; i < numberOfClusters; i++)
                    assignments.put(i, new ArrayList<>());

                List<Integer> currentAssignments = new ArrayList<>();
                htmlDocDe.append(messageSource.getMessage("doc.steps.iteration.start", new Object[]{ iteration + 1}, Locale.GERMAN));
                htmlDocEn.append(messageSource.getMessage("doc.steps.iteration.start",  new Object[]{ iteration + 1}, Locale.ENGLISH));

                //centroid coordinates are given in the first iteration, no calculation needed
                //only assignment in first iteration
                if(iteration == 0){
                    htmlDocDe.append("<li>").append(messageSource.getMessage("doc.steps.recalculate.skip", null, Locale.GERMAN)).append("</li>");
                    htmlDocEn.append("<li>").append(messageSource.getMessage("doc.steps.recalculate.skip", null, Locale.ENGLISH)).append("</li>");
                }
                else{
                    // Recalculate centroids
                    boolean centroidChanged = recalculateCentroids(centroids, snapshots.getLast(), htmlDocDe,
                        htmlDocEn, messageSource, numberOfClusters);

                    if (!centroidChanged)
                        break;
                }
                // Assign data points to nearest centroids and generate HTML
                currentAssignments = assignPointsToCentroids(inputPoints, centroids, assignments,
                    htmlDocDe, htmlDocEn, messageSource, distanceMetric);

                if (iteration == 0){
                    previousAssignments = new ArrayList<>(currentAssignments);
                }

                // Check if at least one point changed assignment
                if (!changed) {
                    for (int i = 0; i < currentAssignments.size(); i++) {
                        if (!Objects.equals(currentAssignments.get(i), previousAssignments.get(i))) {
                            changed = true;
                            break;
                        }
                    }
                }
                previousAssignments = currentAssignments;

                // Snapshot
                List<PointDto> snapshotCentroids = new ArrayList<>(centroids);

                Map<Integer, List<DataPoint>> snapshotAssignments = new HashMap<>();
                for (var e : assignments.entrySet()) {
                    snapshotAssignments.put(e.getKey(), new ArrayList<>(e.getValue()));
                }

                IterationSnapshot snapshot = new IterationSnapshot(snapshotCentroids, snapshotAssignments);
                snapshots.add(snapshot);

                // Show user input format as example
                htmlDocDe.append(String.format(messageSource.getMessage("doc.steps.iteration.result", new Object[]{snapshot.toString()}, Locale.GERMAN)));
                htmlDocEn.append(String.format(messageSource.getMessage("doc.steps.iteration.result", new Object[]{snapshot.toString()}, Locale.ENGLISH)));
            }

            if (!changed) {
                htmlDocDe = new StringBuilder();
                htmlDocEn = new StringBuilder();
                System.out.println("No point changed cluster. Regenerating (attempt " + retries + ")...");
                this.getClusters().clear();
                this.solutionIterations.clear();
                inputPoints = this.generateExercisePoints();
                continue;
            }

            htmlDocDe.append("<li>").append(messageSource.getMessage("doc.steps.converged", null, Locale.GERMAN)).append("</li></ol>");
            htmlDocEn.append("<li>").append(messageSource.getMessage("doc.steps.converged", null, Locale.ENGLISH)).append("</li></ol>");

            this.setSolutionInstructionsDe(htmlDocDe.toString());
            this.setSolutionInstructionsEn(htmlDocEn.toString());

            // Create solution clusters
            IterationSnapshot finalSnapshot = snapshots.getLast();
            for (int i = 0; i < numberOfClusters; i++) {

                Cluster solCluster = new Cluster(ClusterType.SOLUTION,
                    finalSnapshot.centroids.get(i).x(),
                    finalSnapshot.centroids.get(i).y(),
                    "C"+(i+1),
                    this);

                for (DataPoint p : finalSnapshot.assignments.get(i)) {
                    solCluster.getDataPoints().add(new DataPoint(p.getX(), p.getY(),p.getName(), solCluster));
                }
                this.getClusters().add(solCluster);
            }

            // Create exercise clusters
            IterationSnapshot exerciseSnapshot = snapshots.getFirst();

            // Clean up existing exercise clusters
            // then add the new exercise clusters
            this.getClusters().removeIf(c -> c.getType() == ClusterType.EXERCISE);

            for (int i = 0; i < numberOfClusters; i++) {

                Cluster exCluster = new Cluster(ClusterType.EXERCISE,
                    exerciseSnapshot.centroids.get(i).x(),
                    exerciseSnapshot.centroids.get(i).y(),
                    "C"+(i+1),
                    this);

                for (DataPoint p : exerciseSnapshot.assignments.get(i)) {
                    exCluster.getDataPoints().add(new DataPoint(p.getX(), p.getY(),p.getName(), exCluster));
                }
                this.getClusters().add(exCluster);
            }

            // Save all iterations as SolutionIteration entities
            this.getSolutionIterations().clear(); // clean up old ones if any
            for (int i = 0; i < snapshots.size(); i++) {
                IterationSnapshot snap = snapshots.get(i);

                String iterationString = snap.toString();
                SolutionIteration iteration = new SolutionIteration(i, iterationString, this);
                this.getSolutionIterations().add(iteration);
            }

            return;
        }

        throw new IllegalStateException("Failed to generate non-trivial task after " + MAX_RETRIES + " attempts.");
    }
    private boolean recalculateCentroids(
        List<PointDto> centroids,
        IterationSnapshot lastSnapshot,
        StringBuilder htmlDocDe,
        StringBuilder htmlDocEn,
        MessageSource messageSource,
        int numberOfClusters
    ) {
        boolean centroidChanged = false;

        htmlDocDe.append("<li>").append(messageSource.getMessage("doc.steps.recalculate", null, Locale.GERMAN)).append("<ul>");
        htmlDocEn.append("<li>").append(messageSource.getMessage("doc.steps.recalculate", null, Locale.ENGLISH)).append("<ul>");

        for (int i = 0; i < numberOfClusters; i++) {
            List<DataPoint> assigned = lastSnapshot.assignments.get(i);
            if (assigned.isEmpty()) {
                htmlDocDe.append(messageSource.getMessage("doc.steps.noPoints", new Object[]{i + 1}, Locale.GERMAN));
                htmlDocEn.append(messageSource.getMessage("doc.steps.noPoints", new Object[]{i + 1}, Locale.ENGLISH));
                continue;
            }

            double sumX = 0, sumY = 0;
            StringJoiner xValues = new StringJoiner(" + ");
            StringJoiner yValues = new StringJoiner(" + ");

            for (DataPoint p : assigned) {
                sumX += p.getX();
                sumY += p.getY();
                xValues.add(String.valueOf(p.getX()));
                yValues.add(String.valueOf(p.getY()));
            }

            double newX = sumX / assigned.size();
            double newY = sumY / assigned.size();

            PointDto old = centroids.get(i);
            if (newX != old.x() || newY != old.y())
                centroidChanged = true;

            centroids.set(i, new PointDto(newX, newY));

            htmlDocDe.append(messageSource.getMessage(
                "doc.steps.newCentroid", new Object[]{i + 1, newX, newY}, Locale.GERMAN));
            htmlDocDe.append(messageSource.getMessage(
                "doc.steps.centroidFormula", new Object[]{xValues.toString(), sumX, assigned.size(), yValues.toString(), sumY, assigned.size()}, Locale.GERMAN));

            htmlDocEn.append(messageSource.getMessage(
                "doc.steps.newCentroid", new Object[]{i + 1, newX, newY}, Locale.ENGLISH));
            htmlDocEn.append(messageSource.getMessage(
                "doc.steps.centroidFormula", new Object[]{xValues.toString(), sumX, assigned.size(), yValues.toString(), sumY, assigned.size()}, Locale.ENGLISH));
        }

        htmlDocDe.append("</ul></li>");
        htmlDocEn.append("</ul></li>");

        return centroidChanged;
    }

    public List<Integer> assignPointsToCentroids(
        List<DataPoint> inputPoints,
        List<PointDto> centroids,
        Map<Integer, List<DataPoint>> assignments,
        StringBuilder htmlDocDe,
        StringBuilder htmlDocEn,
        MessageSource messageSource,
        DistanceMetric distanceMetric
    ) {
        List<Integer> currentAssignments = new ArrayList<>();

        htmlDocDe.append("<li>").append(messageSource.getMessage("doc.steps.assignments", null, Locale.GERMAN)).append("</li><ul>");
        htmlDocEn.append("<li>").append(messageSource.getMessage("doc.steps.assignments", null, Locale.ENGLISH)).append("</li><ul>");

        for (DataPoint p : inputPoints) {
            double minDist = Double.MAX_VALUE;
            int bestCluster = -1;

            StringBuilder distLineDe = new StringBuilder(
                messageSource.getMessage("doc.steps.assignmentPrefix", new Object[]{p.getName(), p.getX(), p.getY()}, Locale.GERMAN));
            StringBuilder distLineEn = new StringBuilder(
                messageSource.getMessage("doc.steps.assignmentPrefix", new Object[]{p.getName(), p.getX(), p.getY()}, Locale.ENGLISH));

            for (int cIndex = 0; cIndex < centroids.size(); cIndex++) {
                PointDto c = centroids.get(cIndex);
                double dist = switch (distanceMetric) {
                    case EUCLIDEAN -> Math.hypot(p.getX() - c.x(), p.getY() - c.y());
                    case MANHATTAN -> Math.abs(p.getX() - c.x()) + Math.abs(p.getY() - c.y());
                };

                String formula = distanceMetric == DistanceMetric.EUCLIDEAN
                    ? String.format("= √((%d − %.2f)² + (%d − %.2f)²)", p.getX(), c.x(), p.getY(), c.y())
                    : String.format("= |%d − %.2f| + |%d − %.2f|", p.getX(), c.x(), p.getY(), c.y());


                String metricDe = messageSource.getMessage("distanceMetric.EUCLIDEAN", null, Locale.GERMAN);
                String metricEn = messageSource.getMessage("distanceMetric.EUCLIDEAN", null, Locale.ENGLISH);

                distLineDe.append(messageSource.getMessage("doc.steps.assignmentDistance",
                    new Object[]{metricDe, cIndex + 1, formula, dist}, Locale.GERMAN));
                distLineEn.append(messageSource.getMessage("doc.steps.assignmentDistance",
                    new Object[]{metricEn, cIndex + 1, formula, dist}, Locale.ENGLISH));

                //tie-breaker at same distance implicit: first centroid by index will be chosen
                if (dist < minDist) {
                    minDist = dist;
                    bestCluster = cIndex;
                }
            }

            distLineDe.append(messageSource.getMessage("doc.steps.assignmentResult", new Object[]{bestCluster + 1}, Locale.GERMAN));
            distLineEn.append(messageSource.getMessage("doc.steps.assignmentResult", new Object[]{bestCluster + 1}, Locale.ENGLISH));

            htmlDocDe.append(distLineDe);
            htmlDocEn.append(distLineEn);
            assignments.get(bestCluster).add(p);
            currentAssignments.add(bestCluster);
        }

        htmlDocDe.append("</ul>");
        htmlDocEn.append("</ul>");

        return currentAssignments;
    }

    public List<PointDto> assignCentroidsByTaskLength(List<DataPoint> inputPoints) {
        List<PointDto> centroids = new ArrayList<>();
        Random random = new Random();

        switch (taskLength) {
            case SHORT -> {
                List<DataPoint> available = new ArrayList<>(inputPoints);
                for (int i = 0; i < numberOfClusters; i++) {
                    DataPoint p = available.get(random.nextInt(available.size()));
                    centroids.add(new PointDto(p.getX(), p.getY()));
                    available.remove(p);
                }
            }

            case MEDIUM, LONG -> {
                Map<DataPoint, Double> totalDistances = new HashMap<>();

                for (DataPoint p1 : inputPoints) {
                    double sum = 0.0;
                    for (DataPoint p2 : inputPoints) {
                        if (p1 != p2) {
                            double dist = switch (distanceMetric) {
                                case EUCLIDEAN -> Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());
                                case MANHATTAN -> Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
                            };
                            sum += dist;
                        }
                    }
                    totalDistances.put(p1, sum);
                }

                List<DataPoint> mostCentral = totalDistances.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue()) // ascending order = least distance = most ambiguous
                    .limit(numberOfClusters)
                    .map(Map.Entry::getKey)
                    .toList();

                for (DataPoint p : mostCentral) {
                    centroids.add(new PointDto(p.getX(), p.getY()));
                }
            }
        }
        return centroids;

    }

    public String getSolutionIterationsString(){
        if (this.solutionIterations == null || this.solutionIterations.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.solutionIterations.size() -1; i++) {
            String iterationString = solutionIterations.get(i).getIterationString();
            sb.append(iterationString);
            sb.append("<br> -- <br>");

        }
        sb.append(solutionIterations.getLast().getIterationString());
        sb.append("<br>");
        return sb.toString().trim();
    }

    public String getSolutionInstructions(Locale locale) {
        if(locale == Locale.ENGLISH){
            return solutionInstructionsEn;
        }
        else
            return solutionInstructionsDe;
    }

    public String getFullSolutionString() {

        //add visualization
        String fb = ClusteringImageGeneration.generateClusterImage(this, true, ClusteringMessageService.getMessageSource(), Locale.ENGLISH);

        fb += "<b>Solution</b><br>";
        //add solution to feedback
        fb += this.getSolutionIterationsString();

        //add instructions to feedback
        fb += this.getSolutionInstructions(Locale.ENGLISH);

        return fb;
    }
}
