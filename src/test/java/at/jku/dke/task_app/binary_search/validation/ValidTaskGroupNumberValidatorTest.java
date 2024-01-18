package at.jku.dke.task_app.binary_search.validation;

import at.jku.dke.task_app.binary_search.dto.ModifyBinarySearchTaskGroupDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidTaskGroupNumberValidatorTest {

    @Test
    void isValidCorrectOrder() {
        // Arrange
        ValidTaskGroupNumberValidator validTaskGroupNumberValidator = new ValidTaskGroupNumberValidator();
        ModifyBinarySearchTaskGroupDto modifyBinarySearchTaskGroupDto = new ModifyBinarySearchTaskGroupDto(1, 2);

        // Act
        boolean result = validTaskGroupNumberValidator.isValid(modifyBinarySearchTaskGroupDto, null);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidSameValue() {
        // Arrange
        ValidTaskGroupNumberValidator validTaskGroupNumberValidator = new ValidTaskGroupNumberValidator();
        ModifyBinarySearchTaskGroupDto modifyBinarySearchTaskGroupDto = new ModifyBinarySearchTaskGroupDto(2, 2);

        // Act
        boolean result = validTaskGroupNumberValidator.isValid(modifyBinarySearchTaskGroupDto, null);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidIncorrectOrder() {
        // Arrange
        ValidTaskGroupNumberValidator validTaskGroupNumberValidator = new ValidTaskGroupNumberValidator();
        ModifyBinarySearchTaskGroupDto modifyBinarySearchTaskGroupDto = new ModifyBinarySearchTaskGroupDto(2, 1);

        // Act
        boolean result = validTaskGroupNumberValidator.isValid(modifyBinarySearchTaskGroupDto, null);

        // Assert
        assertFalse(result);
    }
}
