package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTask;
import at.jku.dke.etutor.task_app.data.entities.BaseTaskInGroup;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.clustering.config.AppConfig;
import at.jku.dke.task_app.clustering.data.entities.enums.ClusterType;
import at.jku.dke.task_app.clustering.data.entities.enums.DistanceMetric;
import at.jku.dke.task_app.clustering.data.entities.enums.DifficultyLevel;
import at.jku.dke.task_app.clustering.dto.PointDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
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
    @Column(name = "difficulty", nullable = false)
    private DifficultyLevel difficulty;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cluster> clusters = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderId ASC")
    private List<SolutionIteration> solutionIterations = new ArrayList<>();
    public ClusteringTask() {
    }

    public ClusteringTask(Long id, int numberOfClusters,
                          DistanceMetric distanceMetric, DifficultyLevel difficulty) {

        this.setId(id);
        this.numberOfClusters = numberOfClusters;
        this.numberOfDataPoints = 0;
        this.distanceMetric = distanceMetric;
        this.difficulty = difficulty;
    }

    public ClusteringTask(BigDecimal maxPoints, TaskStatus status,
                          int numberOfClusters, DistanceMetric distanceMetric, DifficultyLevel difficulty) {
        super(maxPoints, status);

        this.numberOfClusters = numberOfClusters;
        this.numberOfDataPoints = 0;
        this.distanceMetric = distanceMetric;
        this.difficulty = difficulty;
    }

    public ClusteringTask(Long id, BigDecimal maxPoints, TaskStatus status,
                          int numberOfClusters, DistanceMetric distanceMetric, DifficultyLevel difficulty) {
        super(id, maxPoints, status);

        this.numberOfClusters = numberOfClusters;
        this.numberOfDataPoints = 0;
        this.distanceMetric = distanceMetric;
        this.difficulty = difficulty;
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

    public String getExerciseCentroidsAsString() {
        return getExerciseClusters().stream()
            .map(c -> "(" + format(c.getX()) + ";" + format(c.getY()) + ")")
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

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public List<SolutionIteration> getSolutionIterations() {
        return solutionIterations;
    }

    public void setSolutionIterations(List<SolutionIteration> solutionIterations) {
        this.solutionIterations = solutionIterations;
    }

    public List<DataPoint> generateExercisePoints() {
        // Variablen aus config laden
        int lowerBound = AppConfig.getInt("numberOfDataPoints.difficulty." + difficulty.toString() + ".lowerBound", 8);
        int upperBound = AppConfig.getInt("numberOfDataPoints.difficulty." + difficulty.toString() + ".upperBound", 12);

        Random random = new Random();
        int pointCount = lowerBound + random.nextInt(upperBound - lowerBound + 1); // inclusive upperBound

        this.numberOfDataPoints = pointCount;

        List<DataPoint> points = new ArrayList<>();
        for (int i = 0; i < pointCount; i++) {
            double x = random.nextDouble() * 100;
            double y = random.nextDouble() * 100;

            points.add(new DataPoint(x, y, (char)(((int)'A') + i), null));
        }

        return points;
    }

    public void solve(List<DataPoint> inputPoints) {
        final int MAX_RETRIES = 5;
        int retries = 0;

        while (retries < MAX_RETRIES) {
            retries++;

            //Clean up existing solution clusters
            this.getClusters().removeIf(c -> c.getType() == ClusterType.SOLUTION);

            if (inputPoints.isEmpty() || numberOfClusters <= 0)
                throw new IllegalStateException("Invalid task setup for solving.");

            //choose cluster centroids based on difficulty
            List<PointDto> centroids = this.assignCentroidsByDifficulty(inputPoints);

            List<IterationSnapshot> snapshots = new ArrayList<>();
            boolean changed = false;

            List<Integer> previousAssignments = new ArrayList<>(Collections.nCopies(inputPoints.size(), -1));

            for (int iteration = 0; iteration < 100; iteration++) {
                Map<Integer, List<DataPoint>> assignments = new HashMap<>();
                for (int i = 0; i < numberOfClusters; i++)
                    assignments.put(i, new ArrayList<>());

                List<Integer> currentAssignments = new ArrayList<>();

                for (DataPoint p : inputPoints) {
                    int closest = -1;
                    double minDist = Double.MAX_VALUE;
                    for (int i = 0; i < centroids.size(); i++) {
                        double dist = switch (distanceMetric) {
                            case EUCLIDEAN -> Math.hypot(p.getX() - centroids.get(i).x(), p.getY() - centroids.get(i).y());
                            case MANHATTAN -> Math.abs(p.getX() - centroids.get(i).x()) + Math.abs(p.getY() - centroids.get(i).y());
                        };
                        if (dist < minDist) {
                            minDist = dist;
                            closest = i;
                        }
                    }
                    assignments.get(closest).add(p);
                    currentAssignments.add(closest);
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

                snapshots.add(new IterationSnapshot(snapshotCentroids, snapshotAssignments));

                // Recalculate centroids
                boolean centroidChanged = false;
                for (int i = 0; i < numberOfClusters; i++) {
                    List<DataPoint> assigned = assignments.get(i);
                    if (assigned.isEmpty()) continue;

                    double sumX = 0, sumY = 0;
                    for (DataPoint p : assigned) {
                        sumX += p.getX();
                        sumY += p.getY();
                    }
                    double newX = sumX / assigned.size();
                    double newY = sumY / assigned.size();

                    PointDto old = centroids.get(i);
                    if (newX != old.x() || newY != old.y())
                        centroidChanged = true;

                    centroids.set(i, new PointDto(newX, newY));
                }

                if (!centroidChanged)
                    break;
            }

            if (!changed) {
                System.out.println("No point changed cluster. Regenerating (attempt " + retries + ")...");
                this.getClusters().clear();
                this.generateExercisePoints();
                continue;
            }

            // Create solution clusters
            IterationSnapshot finalSnapshot = snapshots.getLast();
            for (int i = 0; i < numberOfClusters; i++) {

                Cluster solCluster = new Cluster(ClusterType.SOLUTION,
                    finalSnapshot.centroids.get(i).x(),
                    finalSnapshot.centroids.get(i).y(),
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

                String iterationString = snapToString(snap);
                SolutionIteration iteration = new SolutionIteration(i, iterationString, this);
                this.getSolutionIterations().add(iteration);
            }

            return;
        }

        throw new IllegalStateException("Failed to generate non-trivial task after " + MAX_RETRIES + " attempts.");
    }

    private List<PointDto> assignCentroidsByDifficulty(List<DataPoint> inputPoints) {
        List<PointDto> centroids = new ArrayList<>();
        Random random = new Random();

        switch (difficulty) {
            case EASY -> {
                List<DataPoint> available = new ArrayList<>(inputPoints);
                for (int i = 0; i < numberOfClusters; i++) {
                    DataPoint p = available.get(random.nextInt(available.size()));
                    centroids.add(new PointDto(p.getX(), p.getY()));
                    available.remove(p);
                }
            }

            case MEDIUM, HARD -> {
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

    public static String snapToString(IterationSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < snapshot.centroids.size(); i++) {
            PointDto center = snapshot.centroids.get(i);
            List<DataPoint> points = snapshot.assignments.getOrDefault(i, List.of());

            sb.append("[(")
                .append(String.format(Locale.US, "%.2f,%.2f", center.x(), center.y()))
                .append("): ");

            for (int j = 0; j < points.size(); j++) {
                sb.append(points.get(j).getName());
                if (j < points.size() - 1)
                    sb.append(", ");
            }

            sb.append("]");
            if (i < snapshot.centroids.size() - 1)
                sb.append("; ");
        }

        return sb.toString();
    }
}
