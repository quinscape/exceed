package de.quinscape.exceed.runtime.action;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as receiving the current value of a context variable.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ExceedContext
{
    /**
     * Name of the context value to inject for the parameter.
     *
     * @return
     */
    String value();

    boolean required() default true;
}
