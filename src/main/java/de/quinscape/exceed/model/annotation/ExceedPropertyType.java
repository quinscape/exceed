package de.quinscape.exceed.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method with an explicit exceed property type if such a type doesn't follow from the java type declaration.
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceedPropertyType
{
    /**
     * Type name
     * @return type name
     */
    String type();

    /**
     * Type param value
     * @return type param value
     */
    String typeParam() default "";

    boolean required() default false;

    Config[] config() default {};

    int maxLength() default -1;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Config
    {
        /**
         * Configuration name
         *
         * @return
         */
        String name();

        /**
         * Simple Expression value
         *
         * @see de.quinscape.exceed.runtime.util.ExpressionUtil#evaluateSimple(java.lang.String)
         *
         * @return
         */
        String value();
    }
}

