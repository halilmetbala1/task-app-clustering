package at.jku.dke.task_app.clustering.data.entities;

import at.jku.dke.task_app.clustering.dto.PointDto;

import java.util.List;
import java.util.Map;

public class IterationSnapshot {
    public List<PointDto> centroids;
    public Map<Integer, List<DataPoint>> assignments;

    public IterationSnapshot(List<PointDto> centroids, Map<Integer, List<DataPoint>> assignments) {
        this.centroids = centroids;
        this.assignments = assignments;
    }
}
