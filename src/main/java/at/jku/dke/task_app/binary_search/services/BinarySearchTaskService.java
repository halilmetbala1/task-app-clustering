package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTask;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskGroupRepository;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskRepository;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskDto;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

/**
 * This class provides methods for managing {@link BinarySearchTask}s.
 */
@Service
public class BinarySearchTaskService extends BaseTaskService<BinarySearchTask, BinarySearchTaskGroup, ModifyBinarySearchTaskDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link BinarySearchTaskService}.
     *
     * @param repository          The task repository.
     * @param taskGroupRepository The task group repository.
     * @param messageSource       The message source.
     */
    public BinarySearchTaskService(BinarySearchTaskRepository repository, BinarySearchTaskGroupRepository taskGroupRepository, MessageSource messageSource) {
        super(repository, taskGroupRepository);
        this.messageSource = messageSource;
    }

    @Override
    protected BinarySearchTask createTask(long id, ModifyTaskDto<ModifyBinarySearchTaskDto> modifyTaskDto) {
        return new BinarySearchTask(modifyTaskDto.additionalData().solution());
    }

    @Override
    protected void updateTask(BinarySearchTask task, ModifyTaskDto<ModifyBinarySearchTaskDto> modifyTaskDto) {
        task.setSolution(modifyTaskDto.additionalData().solution());
    }

    @Override
    protected Serializable mapToReturnData(BinarySearchTask task, boolean create) {
        // generate default task description
        var map = new HashMap<String, String>();
        map.put("en", this.messageSource.getMessage("defaultDescription", new Object[]{task.getTaskGroup().getMinNumber(), task.getTaskGroup().getMaxNumber()}, Locale.ENGLISH));
        map.put("de", this.messageSource.getMessage("defaultDescription", new Object[]{task.getTaskGroup().getMinNumber(), task.getTaskGroup().getMaxNumber()}, Locale.GERMAN));
        return map;
    }

}
