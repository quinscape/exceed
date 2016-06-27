package de.quinscape.exceed.model.view;


import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.TokenMgrError;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.JSONable;

import java.util.Collection;
import java.util.Map;

/**
 * Encapsulates an attribute value that is either a simple string or an expression string. An expression string starts with
 * <code>'{'</code> and ends with <code>'}'</code>
 *
 * If the expression in the attribute is syntactically invalid, did not produce a valid abstract syntax tree, the type will
 * be {@link AttributeValueType#EXPRESSION_ERROR} and the AST returned will be null.
 *
 * In this case, {@link #getExpressionError()} will return the error that occured evaluating the attribute.
 *
 * @see ExpressionParser
 */
public class AttributeValue
    implements JSONable
{
    private final AttributeValueType type;

    private final String value;

    private final ASTExpression astExpression;

    private final Throwable expressionError;

    private final static JSON generator = SingleQuoteJSONGenerator.INSTANCE;

    private AttributeValue(
        AttributeValueType type,
        final String value,
        boolean forceExpression)
    {

        if (type == AttributeValueType.EXPRESSION)
        {
            ASTExpression astExpression;
            Throwable exception;
            try
            {
                astExpression = ExpressionParser.parse(forceExpression ? value : value.substring(1, value.length() - 1));
                exception = null;
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

        this.value = value;
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


    /**
     * Creates an attribute value for the given string value. If the string value
     * starts with <code>'{'</code> and ends with <code>'}'</code> the resulting attribute value
     * will have {@link AttributeValueType#EXPRESSION } (or {@link AttributeValueType#EXPRESSION_ERROR } if it is an invalid
     * expression )
     *
     * @param value string or expression string
     * @return  attribute value
     */
    public static AttributeValue forValue(String value, boolean forceExpression)
    {
        if (value == null)
        {
            return null;
        }

        String stringValue = (String) value;
        if (forceExpression)
        {
            return new AttributeValue(AttributeValueType.EXPRESSION, stringValue, forceExpression);
        }
        else if (stringValue.startsWith("{") && stringValue.endsWith("}"))
        {
            return new AttributeValue(AttributeValueType.EXPRESSION, formatExpression(stringValue), false);
        }
        else
        {
            return new AttributeValue(AttributeValueType.STRING, value, false);
        }
    }


    /**
     * Returns a number expression attribute value for the given number.
     *
     * @param value number
     * @return number expression attribute value
     */
    public static AttributeValue forValue(Number value)
    {
        return toExpression(value);
    }

    /**
     * Returns a map expression attribute value for the given map.
     *
     * @param value map
     * @return map expression attribute value
     */
    public static AttributeValue forValue(Map value)
    {
        return toExpression(value);
    }

    /**
     * Returns a list expression attribute value for the given collection.
     *
     * @param value collection
     * @return list expression attribute value
     */
    public static AttributeValue forValue(Collection value)
    {
        return toExpression(value);
    }

    /**
     * Returns a boolean expression attribute value for the given boolean.
     *
     * @param value boolean value
     * @return boolean expression attribute value
     */
    public static AttributeValue forValue(Boolean value)
    {
        return toExpression(value);
    }


    /**
     * Internal method to convert an Object to an attribute value.
     *
     * @param value value
     * @return
     */
    static AttributeValue toExpression(Object value)
    {
        return new AttributeValue(AttributeValueType.EXPRESSION, "{ " + generator.forValue(value) + " }", false);
    }

    static String formatExpression(String expr)
    {
        if (expr.charAt(1) != ' ')
        {
            expr = "{ " + expr.substring(1);
        }

        int closingBrace = expr.length() - 1;
        int beforeBrace = expr.length() - 2;
        if (expr.charAt(beforeBrace) != ' ')
        {
            expr = expr.substring(0,closingBrace) + " }";
        }

        return expr;
    }
}

