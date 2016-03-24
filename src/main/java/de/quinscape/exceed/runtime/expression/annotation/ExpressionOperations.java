package de.quinscape.exceed.runtime.expression.annotation;

import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta annotation marking Spring components that provide operations and identifiers for expression environment
 *
 * <pre>
 *     \@ExpressionOperations( enviropment = MyEnv.class)
 *     public class MyExpressionExtension
 *     {
 *         \@Operation
 *         public Object myOperation(ExpressionContext<MyEnv> ctx, String param)
 *         {
 *             ...
 *         }

 *         \@Operation(context = MyOperationContext.class)
 *         public Object myOperation(ExpressionContext<MyEnv> ctx, MyOperationContext context, String param)
 *         {
 *             ...
 *         }
 *
 *         \@Identifier
 *         public String myIdentifier(MyEnv env)
 *         {
 *              return env.getMyIdentifierValue();
 *         }
 *
 *     }
 * </pre>
 *
 * @see Operation
 * @see Identifier
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ExpressionOperations
{
    Class<? extends ExpressionEnvironment> environment();
}
