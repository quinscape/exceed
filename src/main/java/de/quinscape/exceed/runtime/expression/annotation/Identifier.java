package de.quinscape.exceed.runtime.expression.annotation;

import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method in a {@link ExpressionOperations} as identifier method.
 *
 * The method must return the value for the identifier and can optionally
 * take the expression environment as parameter
 * <p>
 *     Alternatively you can override {@link ExpressionEnvironment#resolveIdentifier(String)} to as non-declarative way of resolving identifiers.
 * </p>
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Identifier
{
    /**
     * Identifier name. If not given, the method name is used.
     *
     * @return identifier name
     */
    String name() default "";
}

