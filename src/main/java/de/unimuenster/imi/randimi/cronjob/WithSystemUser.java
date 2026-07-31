package de.unimuenster.imi.randimi.cronjob;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Executes the annotated method as the system user.
 * Implemented by {@link WithSystemUserAspect#withSystemUser(ProceedingJoinPoint)}
 *
 * @author Daniel Preciado-Marquez
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithSystemUser {
}
