package at.jku.dke.task_app.clustering.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "data_point")
public class DataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dp_seq")
    @SequenceGenerator(name = "dp_seq", sequenceName = "data_point_id_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private double x;

    @NotNull
    private double y;

    @NotNull
    private char name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private Cluster cluster;

    public DataPoint() {}

    public DataPoint(double x, double y, char name, Cluster cluster) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.cluster = cluster;
    }

    public Long getId() {
        return id;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
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

    public char getName() {
        return name;
    }

    public void setName(char name) {
        this.name = name;
    }
}
