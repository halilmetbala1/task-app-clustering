package at.jku.dke.task_app.clustering.data.repositories;

import at.jku.dke.task_app.clustering.data.entities.DataPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataPointRepository extends JpaRepository<DataPoint, Long> {
}
