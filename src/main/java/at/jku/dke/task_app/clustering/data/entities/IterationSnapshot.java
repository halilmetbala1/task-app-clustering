package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.task_app.clustering.dto.PointDto;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IterationSnapshot {
    public List<PointDto> centroids;
    public Map<Integer, List<DataPoint>> assignments;

    public IterationSnapshot(List<PointDto> centroids, Map<Integer, List<DataPoint>> assignments) {
        this.centroids = centroids;
        this.assignments = assignments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.centroids.size(); i++) {
            PointDto center = this.centroids.get(i);
            List<DataPoint> points = this.assignments.getOrDefault(i, List.of());

            sb.append("[(")
                .append(String.format(Locale.US, "%.2f,%.2f", center.x(), center.y()))
                .append("): ");

            for (int j = 0; j < points.size(); j++) {
                sb.append(points.get(j).getName());
                if (j < points.size() - 1)
                    sb.append(", ");
            }

            sb.append("]");
            if (i < this.centroids.size() - 1)
                sb.append("; ");
        }

        return sb.toString();
    }
}
