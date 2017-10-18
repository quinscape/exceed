package de.quinscape.exceed.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a meta data method as to receive the current resource contents of a declared resource
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectResource
{
    /**
     * Path of the resource to inject into that property.
     *
     * @return resource path
     */
    String value();
}
