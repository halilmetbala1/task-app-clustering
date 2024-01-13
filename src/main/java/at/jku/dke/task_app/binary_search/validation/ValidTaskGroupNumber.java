package at.jku.dke.task_app.binary_search.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * The annotated elements numbers must be in ascending order.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidTaskGroupNumberValidator.class})
public @interface ValidTaskGroupNumber {
    String message() default "{invalidTaskGroupNumber}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
