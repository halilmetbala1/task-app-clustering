package at.jku.dke.task_app.clustering.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "data_point")
public class DataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dp_seq")
    @SequenceGenerator(name = "dp_seq", sequenceName = "data_point_id_seq", allocationSize = 1)
    private Long id;

    @NotNull
    private int x;

    @NotNull
    private int y;

    @NotNull
    private char name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private Cluster cluster;

    public DataPoint() {}

    public DataPoint(int x, int y, char name, Cluster cluster) {
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public char getName() {
        return name;
    }

    public void setName(char name) {
        this.name = name;
    }

    public static String renderDataPointsTable(List<DataPoint> dataPoints) {
        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellpadding='5' cellspacing='0'>");
        html.append("<tr><th>Name</th><th>X</th><th>Y</th></tr>");

        for (DataPoint p : dataPoints.stream().sorted(Comparator.comparing(DataPoint::getName)).toList()) {
            html.append("<tr>")
                .append("<td>").append(p.getName()).append("</td>")
                .append("<td>").append(p.getX()).append("</td>")
                .append("<td>").append(p.getY()).append("</td>")
                .append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }
}
