package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;

public class ExpressionConverter
    implements PropertyConverter<ASTExpression,Object,ASTExpression>
{
    @Override
    public ASTExpression convertToJava(RuntimeContext runtimeContext, Object value)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public ASTExpression convertToJSON(RuntimeContext runtimeContext, ASTExpression value)
    {
        throw new UnsupportedOperationException();
    }



    @Override
    public ASTExpression convertToJs(RuntimeContext runtimeContext, ASTExpression value)
    {
        return value;
    }


    @Override
    public ASTExpression convertFromJs(RuntimeContext runtimeContext, ASTExpression value)
    {
        return value;
    }


    @Override
    public Class<ASTExpression> getJavaType()
    {
        return ASTExpression.class;
    }


    @Override
    public Class<Object> getJSONType()
    {
        return Object.class;
    }
}
