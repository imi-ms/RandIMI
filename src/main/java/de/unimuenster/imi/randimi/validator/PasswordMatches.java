package de.unimuenster.imi.randimi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {
	String message() default "Password don't match!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
