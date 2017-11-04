package de.quinscape.exceed.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the description used in the model documentation for collection properties. For maps you can configure the key and
 * the value documentation (e.g. "Map myName -> MyType"), for lists and sets the value documentation (Array of MyType)
 * 
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocumentedCollection
{
    /**
     * Description to use for the map key
     */
    String keyDesc() default "";

    /**
     * Description to use for the map value
     */
    String valueDesc() default "";
}
