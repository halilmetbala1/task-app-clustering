package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskGroupRepository;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskRepository;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link BinarySearchTask}s.
 */
@Service
public class BinarySearchTaskService extends BaseTaskService<BinarySearchTask, BinarySearchTaskGroup, ModifyBinarySearchTaskDto> {
    /**
     * Creates a new instance of class {@link BinarySearchTaskService}.
     *
     * @param repository          The task repository.
     * @param taskGroupRepository The task group repository.
     */
    protected BinarySearchTaskService(BinarySearchTaskRepository repository, BinarySearchTaskGroupRepository taskGroupRepository) {
        super(repository, taskGroupRepository);
    }

    @Override
    protected BinarySearchTask createTask(long id, ModifyTaskDto<ModifyBinarySearchTaskDto> modifyTaskDto) {
        return new BinarySearchTask(modifyTaskDto.additionalData().solution());
    }

    @Override
    protected void updateTask(BinarySearchTask task, ModifyTaskDto<ModifyBinarySearchTaskDto> modifyTaskDto) {
        task.setSolution(modifyTaskDto.additionalData().solution());
    }
}
