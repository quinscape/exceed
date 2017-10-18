package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ModelValidationContext
{
    private final static Logger log = LoggerFactory.getLogger(ModelValidationContext.class);


    private final Set<ApplicationError> errors;

    public ModelValidationContext()
    {
        errors = new HashSet<>();
    }


    public Set<ApplicationError> getErrors()
    {
        return errors;
    }

    public void registerError(ExpressionModelContext expressionModelContext, ASTExpression astExpression, Throwable t)
    {
        registerError(expressionModelContext, astExpression, t.getMessage());

        log.error("ModelValidation error:", t);
    }
    public void registerError(ExpressionModelContext expressionModelContext, ASTExpression astExpression, String message)
    {
        errors.add(
            new ApplicationError(
                expressionModelContext,
                astExpression,
                message
            )
        );
    }
}
