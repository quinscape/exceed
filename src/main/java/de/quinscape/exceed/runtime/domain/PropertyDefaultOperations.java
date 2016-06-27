package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@ExpressionOperations(environment = PropertyDefaultEnvironment.class)
public class PropertyDefaultOperations
{

    @Operation
    public Object now(ExpressionContext<PropertyDefaultEnvironment> ctx)
    {
        return Timestamp.from(Instant.now());
    }

    @Operation
    public Object newObject(ExpressionContext<PropertyDefaultEnvironment> ctx)
    {
        final PropertyDefaultEnvironment env = ctx.getEnv();

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

        return env.getDomainService().create(type, UUID.randomUUID().toString());
    }

}
