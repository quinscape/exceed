package de.quinscape.exceed.model.annotation;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.process.Process;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the model docs generator how to document the possible types of a model property. Normally the @JSONTypeHint
 * annotated type hint is documented itself as nested type documentation. This annotations defines an array of types
 * to document instead. (e.g. {@link Process#getStates()} documents the concrete state types and not the ProcessState base
 * class)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocumentedSubTypes
{
    Class<? extends Model>[] value();
}
