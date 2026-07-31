package de.unimuenster.imi.randimi.validator.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsernameConstraint {
	String message() default "{validator.user.username.taken}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
