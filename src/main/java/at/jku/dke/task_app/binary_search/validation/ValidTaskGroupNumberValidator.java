package at.jku.dke.task_app.binary_search.validation;

import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom validator for numbers in {@link ModifyBinarySearchTaskGroupDto}.
 */
public class ValidTaskGroupNumberValidator implements ConstraintValidator<ValidTaskGroupNumber, ModifyBinarySearchTaskGroupDto> {
    /**
     * Creates a new instance of class Valid task group number validator.
     */
    public ValidTaskGroupNumberValidator() {
    }

    @Override
    public boolean isValid(ModifyBinarySearchTaskGroupDto value, ConstraintValidatorContext context) {
        return value.minNumber() < value.maxNumber();
    }
}
