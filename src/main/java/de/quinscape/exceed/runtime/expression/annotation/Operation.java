package de.quinscape.exceed.runtime.expression.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks methods in {@link ExpressionOperations}s as expression operation method.
 *
 * The method must take ExpressionContext<T> as first parameter where <T> should correspond
 * to the environment class given in the {@link ExpressionOperations} annotation.
 *
 * The second parameter can be the operation context if applicable. All following parameters
 * receive the evaluation result of the underlying ASTFunction parameters.
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Operation
{
    /**
     * Operation type to apply this operation to. Default is no context.
     *
     * An object with the given type will be injected as second parameter if possible
     *
     * @return
     */
    Class<?> context() default void.class;

    String name() default "";
}

