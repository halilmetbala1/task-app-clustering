package at.jku.dke.task_app.clustering.data.entities;

import jakarta.persistence.*;

@Entity
public class SolutionIteration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int orderId;

    @Column(columnDefinition = "TEXT")
    private String iterationString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ClusteringTask task;

    public SolutionIteration() {
    }

    public SolutionIteration(int orderId, String iterationString, ClusteringTask task) {
        this.orderId = orderId;
        this.iterationString = iterationString;
        this.task = task;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getIterationString() {
        return iterationString;
    }

    public void setIterationString(String iterationString) {
        this.iterationString = iterationString;
    }

    public ClusteringTask getTask() {
        return task;
    }

    public void setTask(ClusteringTask task) {
        this.task = task;
    }
}
