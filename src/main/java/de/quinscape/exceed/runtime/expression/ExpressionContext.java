package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

/**
 * Context class for expression environment operation invocations
 *
 * @param <T>   expression environment
 */
public final class ExpressionContext<T extends ExpressionEnvironment>
{
    private final T env;

    private final ASTFunction astFunction;

    public ExpressionContext(T env, ASTFunction astFunction)
    {
        this.env = env;
        this.astFunction = astFunction;
    }


    /**
     * Returns the expression environment in which this operation is invoked.
     *
     * @return  environment
     */
    public T getEnv()
    {
        return env;
    }


    /**
     * Returns the original ASTFunction underlying the operation for manual evaluation.
     *
     * @return  AST function
     */
    public ASTFunction getASTFunction()
    {
        return astFunction;
    }
}

