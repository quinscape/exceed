package de.quinscape.exceed.model.view;


import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import org.svenson.JSON;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONable;

public class AttributeValue
    implements JSONable
{
    private final AttributeValueType type;

    private final Object value;

    private final ASTExpression astExpression;


    public AttributeValue(
        AttributeValueType type,
        Object value) throws ParseException
    {
        this.type = type;
        this.value = value;

        if (type == AttributeValueType.EXPRESSION)
        {
            String expr = (String) value;
            this.astExpression = ExpressionParser.parse(expr.substring(1, expr.length() - 1));
        }
        else
        {
            this.astExpression = null;
        }

    }


    public AttributeValueType getType()
    {
        return type;
    }


    public Object getValue()
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

