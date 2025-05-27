package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.task_app.clustering.data.entities.enums.ClusterType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cluster")
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cluster_seq")
    @SequenceGenerator(name = "cluster_seq", sequenceName = "cluster_id_seq", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ClusterType type;

    //coordinates represent the centroid of the cluster
    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    private double y;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ClusteringTask task;

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DataPoint> dataPoints = new ArrayList<>();

    public Cluster() {}

    public Cluster(Long id, ClusterType type, double x, double y, ClusteringTask task) {
        this.id = id;
        this.type = type;
        this.task = task;
        this.x = x;
        this.y = y;
    }
    public Cluster(ClusterType type, double x, double y, ClusteringTask task) {
        this.type = type;
        this.task = task;
        this.x = x;
        this.y = y;
    }

    public ClusterType getType() {
        return type;
    }

    public void setType(ClusterType type) {
        this.type = type;
    }

    public ClusteringTask getTask() {
        return task;
    }

    public void setTask(ClusteringTask task) {
        this.task = task;
    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }
}
