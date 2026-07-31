package de.unimuenster.imi.randimi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordCorrectValidator.class)
@Documented
public @interface PasswordCorrect {
	String passwordFieldName();

	String message() default "Password is not correct!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
