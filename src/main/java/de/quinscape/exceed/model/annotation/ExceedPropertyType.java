package de.quinscape.exceed.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a model type as internal, to be ignored by {@link de.quinscape.exceed.tooling.GenerateModelDocs}
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceedPropertyType
{
    String type();
    String typeParam() default "";
}

