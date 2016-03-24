package de.quinscape.exceed.model.view;


import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.TokenMgrError;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.JSONable;

public class AttributeValue
    implements JSONable
{
    private final AttributeValueType type;

    private final String value;

    private final ASTExpression astExpression;

    private final Throwable expressionError;

    public AttributeValue(
        AttributeValueType type,
        String value) throws ParseException
    {
        this.value = value;

        if (type == AttributeValueType.EXPRESSION)
        {
            String expr = (String) value;
            ASTExpression astExpression = null;
            Throwable exception = null;
            try
            {
                astExpression = ExpressionParser.parse(expr.substring(1, expr.length() - 1));
            }
            catch(TokenMgrError | ParseException e)
            {
                type = AttributeValueType.EXPRESSION_ERROR;
                astExpression = null;
                exception = e;
            }

            this.expressionError = exception;
            this.astExpression = astExpression;
        }
        else
        {
            this.astExpression = null;
            this.expressionError = null;
        }
        this.type = type;
    }


    public AttributeValueType getType()
    {
        return type;
    }



    @JSONProperty(ignore = true)
    public Throwable getExpressionError()
    {
        return expressionError;
    }


    public String getValue()
    {
        return value;
    }


    /**
     * Returns the cached expression AST tree for this attribute if the attribute is an attribute of
     * type expression.
     *
     * @return ast expression tree or <code>null</code>
     */
    public ASTExpression getAstExpression()
    {
        return astExpression;
    }


    @Override
    public String toJSON()
    {
        return JSON.defaultJSON().forValue(value);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = " + type
            + ", value = '" + value + '\''
            ;
    }
}

