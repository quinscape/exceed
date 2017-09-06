package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionParameters;
import de.quinscape.exceed.runtime.action.ActionRegistration;
import de.quinscape.exceed.runtime.expression.ExpressionContext;

import java.util.ArrayList;
import java.util.List;

public class QueryTransformerActionParameters
    implements ActionParameters
{
    private final ExpressionContext<QueryTransformerEnvironment> ctx;

    private final ASTFunction astFunction;

    public QueryTransformerActionParameters(ExpressionContext<QueryTransformerEnvironment> ctx, ASTFunction astFunction)
    {
        this.ctx = ctx;
        this.astFunction = astFunction;
    }


    @Override
    public List<Object> get(
        RuntimeContext runtimeContext, ActionRegistration registration, List<DomainProperty> propertyModels
    )
    {
        final int numChildren = astFunction.jjtGetNumChildren();
        List<Object> list = new ArrayList<>(numChildren - 1);
        for (int i = 1; i < numChildren; i++)
        {
            final Object value = astFunction.jjtGetChild(i).jjtAccept(ctx.getEnv(), null);
            list.add(value);                                                
        }
        return list;
    }
}
