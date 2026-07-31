package de.unimuenster.imi.randimi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrongValidator.class)
@Documented
public @interface PasswordStrong {
	String message() default "Password is not strong enough!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
