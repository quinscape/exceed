package de.quinscape.exceed.model.annotation;

import de.quinscape.exceed.model.merge.MergeType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the merge strategy to use for this model and its properties. The property methods might contain
 * further strategy hint modifying or ending the parent merge strategy.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MergeStrategy
{
    MergeType value() default MergeType.REPLACE;
}
