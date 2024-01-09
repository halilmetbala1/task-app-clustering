package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.services.BaseTaskGroupService;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskGroupRepository;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link BinarySearchTaskGroup}s.
 */
@Service
public class BinarySearchTaskGroupService extends BaseTaskGroupService<BinarySearchTaskGroup, ModifyBinarySearchTaskGroupDto> {

    /**
     * Creates a new instance of class {@link BinarySearchTaskGroupService}.
     *
     * @param repository The task group repository.
     */
    public BinarySearchTaskGroupService(BinarySearchTaskGroupRepository repository) {
        super(repository);
    }

    @Override
    protected BinarySearchTaskGroup createTaskGroup(long id, ModifyTaskGroupDto<ModifyBinarySearchTaskGroupDto> modifyTaskGroupDto) {
        return new BinarySearchTaskGroup(modifyTaskGroupDto.additionalData().minNumber(), modifyTaskGroupDto.additionalData().maxNumber());
    }

    @Override
    protected void updateTaskGroup(BinarySearchTaskGroup taskGroup, ModifyTaskGroupDto<ModifyBinarySearchTaskGroupDto> modifyTaskGroupDto) {
        taskGroup.setMinNumber(modifyTaskGroupDto.additionalData().minNumber());
        taskGroup.setMaxNumber(modifyTaskGroupDto.additionalData().maxNumber());
    }

}
