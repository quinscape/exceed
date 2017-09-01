package de.quinscape.exceed.runtime.action;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for action methods.
 *
 * @see CustomLogic
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Action
{
    String METHOD_NAME = "$methodName$";

    /**
     * Returns the name of the action. As a default, the name of the annotated method is used.
     *
     * @return name of the action
     */
    String value() default METHOD_NAME;

    String[] description() default {};
    
    /**
     * Returns the action environment in which this action is valid. Default value is {@link ActionEnvironment#SERVER_AND_CLIENT}.
     */
    ActionEnvironment env() default ActionEnvironment.SERVER_AND_CLIENT;
}
