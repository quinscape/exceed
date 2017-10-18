package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

public class ApplicationError
{

    private final ExpressionModelContext expressionModelContext;

    private final ASTExpression astExpression;

    private final String message;


    public ApplicationError(
        ExpressionModelContext expressionModelContext, ASTExpression astExpression,
        String message
    )
    {
        this.expressionModelContext = expressionModelContext;
        this.astExpression = astExpression;
        this.message = message;
    }


    public ASTExpression getAstExpression()
    {
        return astExpression;
    }


    public String getMessage()
    {
        return message;
    }


    public ExpressionModelContext getExpressionModelContext()
    {
        return expressionModelContext;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "expressionModelContext = " + expressionModelContext
            + ", astExpression = " + astExpression
            + ", message = '" + message + '\''
            ;
    }
}
