package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@ExpressionOperations(environment = ContextExpressionEnvironment.class)
public class ContextExpressionOperations
{
    @Operation
    public Object param(ExpressionContext<ContextExpressionEnvironment> ctx, String name)
    {
        Map<String, Object> params = ctx.getEnv().getParams();

        if (params == null)
        {
            throw new IllegalStateException("Context has no parameters");
        }

        return params.get(name);
    }

    @Operation
    public Object newObject(ExpressionContext<ContextExpressionEnvironment> ctx)
    {
        final ContextExpressionEnvironment env = ctx.getEnv();
        ScopedContextChain scopedContext = env.getRuntimeContext().getScopedContextChain();
        if (scopedContext == null)
        {
            throw new IllegalStateException("Scoped context not initialized, can't access  process property");
        }

        final ASTFunction fn = ctx.getASTFunction();
        String type = null;
        if (fn.jjtGetNumChildren() == 1)
        {
            final Node node = fn.jjtGetChild(0);
            if (!(node instanceof ASTString))
            {
                throw new IllegalStateException("newObject argument is no string literal");
            }

            type = ((ASTString) node).getValue();
        }

        if (type == null)
        {

            final ScopedPropertyModel propertyModel = env.getScopedPropertyModel();
            if (!propertyModel.getType().equals(DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE))
            {
                throw new IllegalStateException("Can only create new objects for property models of type '" + DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE + "': " + propertyModel);
            }

            type = (String) propertyModel.getTypeParam();
        }

        return env.getRuntimeContext().getDomainService().create(type, UUID.randomUUID().toString());
    }

    @Operation
    public Object now(ExpressionContext<ContextExpressionEnvironment> ctx)
    {
        return Timestamp.from(Instant.now());
    }

}
