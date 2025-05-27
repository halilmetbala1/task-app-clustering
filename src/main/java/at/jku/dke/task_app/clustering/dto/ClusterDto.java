package at.jku.dke.task_app.clustering.dto;

import at.jku.dke.task_app.clustering.data.entities.Cluster;

import java.util.List;

/**
 * Represents a cluster with its type and associated points.
 *
 * @param type   Cluster type (EXERCISE or SOLUTION)
 * @param points The list of points in the cluster
 */
public record ClusterDto(String type, List<PointDto> points) {
    public static ClusterDto fromCluster(Cluster cluster) {
        return new ClusterDto(
            cluster.getType() != null ? cluster.getType().name() : "UNKNOWN",
            cluster.getDataPoints().stream()
                .map(p -> new PointDto(p.getX(), p.getY()))
                .toList()
        );
    }
}
