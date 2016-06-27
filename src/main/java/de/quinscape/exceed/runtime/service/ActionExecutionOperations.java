package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@ExpressionOperations(environment = ActionExecutionEnvironment.class)
public class ActionExecutionOperations
{
    @Operation
    public DomainObject object(ExpressionContext<ActionExecutionEnvironment> ctx, String name)
    {
        return ctx.getEnv().getScopedContext().getObject(name);
    }

    @Operation
    public DataList list(ExpressionContext<ActionExecutionEnvironment> ctx, String name)
    {
        return ctx.getEnv().getScopedContext().getList(name);
    }

    @Operation
    public Object property(ExpressionContext<ActionExecutionEnvironment> ctx, String name)
    {
        return ctx.getEnv().getScopedContext().getProperty(name);
    }

    @Operation
    public Object now(ExpressionContext<ActionExecutionEnvironment> ctx)
    {
        return Timestamp.from(Instant.now());
    }

    @Operation
    public Object newObject(ExpressionContext<ActionExecutionEnvironment> ctx)
    {
        final ActionExecutionEnvironment env = ctx.getEnv();

        final ASTFunction fn = ctx.getASTFunction();
        String type = null;
        if (fn.jjtGetNumChildren() != 1)
        {
            throw new IllegalArgumentException("newObj takes exactly one argument ( domainType)");
        }
        if (!(fn.jjtGetChild(0) instanceof ASTString))
        {
            throw new IllegalArgumentException("newObj argument is not a string literal ( domainType)");
        }

        type = ((ASTString) fn.jjtGetChild(0)).getValue();

        return env.getRuntimeContext().getDomainService().create(type, UUID.randomUUID().toString());
    }
}
