package at.jku.dke.task_app.clustering.data.repositories;

import at.jku.dke.etutor.task_app.data.repositories.TaskRepository;
import at.jku.dke.task_app.clustering.data.entities.Cluster;
import at.jku.dke.task_app.clustering.data.entities.ClusteringTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterRepository extends JpaRepository<Cluster, Long> {
}
