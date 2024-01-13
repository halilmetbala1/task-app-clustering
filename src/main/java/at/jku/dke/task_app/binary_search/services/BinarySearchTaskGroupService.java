package at.jku.dke.task_app.binary_search.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskGroupModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskGroupService;
import at.jku.dke.task_app.binary_search.data.entities.BinarySearchTaskGroup;
import at.jku.dke.task_app.binary_search.data.repositories.BinarySearchTaskGroupRepository;
import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * This class provides methods for managing {@link BinarySearchTaskGroup}s.
 */
@Service
public class BinarySearchTaskGroupService extends BaseTaskGroupService<BinarySearchTaskGroup, ModifyBinarySearchTaskGroupDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link BinarySearchTaskGroupService}.
     *
     * @param repository    The task group repository.
     * @param messageSource The message source.
     */
    public BinarySearchTaskGroupService(BinarySearchTaskGroupRepository repository, MessageSource messageSource) {
        super(repository);
        this.messageSource = messageSource;
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

    @Override
    protected TaskGroupModificationResponseDto mapToReturnData(BinarySearchTaskGroup taskGroup, boolean create) {
        return new TaskGroupModificationResponseDto(
            this.messageSource.getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getMinNumber(), taskGroup.getMaxNumber()}, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getMinNumber(), taskGroup.getMaxNumber()}, Locale.ENGLISH));
    }
}
