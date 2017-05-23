package de.quinscape.exceed.model.expression;


import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.TokenMgrError;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.JSONable;

/**
 * Encapsulates an attribute value that is either a simple string or an expression string. An expression string
 * starts with
 * <code>'{'</code> and ends with <code>'}'</code>
 * <p>
 * If the expression in the attribute is syntactically invalid, did not produce a valid abstract syntax tree, the
 * type will
 * be {@link ExpressionValueType#EXPRESSION_ERROR} and the AST returned will be null.
 * <p>
 * In this case, {@link #getExpressionError()} will return the error that occurred evaluating the attribute.
 *
 * @see ExpressionParser
 */
public class ExpressionValue
    implements JSONable
{
    private final ExpressionValueType type;

    private final String value;

    private final ASTExpression astExpression;

    private final Throwable expressionError;

    private final static JSON generator = SingleQuoteJSONGenerator.INSTANCE;


    /**
     * Private constructor.
     *
     * @see Attributes
     * @see ExpressionValue#forValue(String, boolean)
     *
     * @param type              expression type
     * @param value             expression string or string literal
     * @param forceExpression   if <code>true</code>, the expression must always be an expression. If <code>false</code>,
     *                          the expression will either be a string literal or marked with <code>"{ … }"</code>
     */
    private ExpressionValue(
        ExpressionValueType type,
        final String value,
        boolean forceExpression)
    {

        if (type == ExpressionValueType.EXPRESSION)
        {
            ASTExpression astExpression;
            Throwable exception;
            try
            {
                astExpression = ExpressionParser.parse(forceExpression ? value : value.substring(1, value.length() -
                    1));
                exception = null;
            }
            catch (TokenMgrError | ParseException e)
            {
                type = ExpressionValueType.EXPRESSION_ERROR;
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

    private ExpressionValue(ASTExpression expression, boolean forceExpression)
    {

        final String value = ExpressionRenderer.render(expression);
        this.value = forceExpression ? value : "{ " + value + " }";
        this.astExpression = expression;
        this.type = ExpressionValueType.EXPRESSION;
        expressionError = null;
    }


    /**
     * Returns the enum type of the attribute.
     *
     * @return type
     */
    public ExpressionValueType getType()
    {
        return type;
    }


    @JSONProperty(ignore = true)
    public Throwable getExpressionError()
    {
        return expressionError;
    }


    /**
     * Returns the string representation of this attribute as-is.
     *
     * @return
     */
    public String getValue()
    {
        return value;
    }


    /**
     * Returns the cached expression AST tree for this expression value if the expression value has the type expression.
     *
     * @return AST expression tree or <code>null</code>
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
     * will have {@link ExpressionValueType#EXPRESSION } (or {@link ExpressionValueType#EXPRESSION_ERROR } if it is
     * an invalid
     * expression )
     *
     * @param value string or expression string
     * @return attribute value
     */
    public static ExpressionValue forValue(String value, boolean forceExpression)
    {
        if (value == null)
        {
            return null;
        }

        if (forceExpression)
        {
            return new ExpressionValue(ExpressionValueType.EXPRESSION, value, true);
        }
        else if (value.startsWith("{") && value.endsWith("}"))
        {
            return new ExpressionValue(ExpressionValueType.EXPRESSION, formatExpression(value), false);
        }
        else
        {
            return new ExpressionValue(ExpressionValueType.STRING, value, false);
        }
    }


    /**
     * Converts an existing expression into an ExpressionValue.
     *
     * @param expression        expression
     * @param forceExpression   if <code>false</code>>, the expression will be enclosed in <code>"{ … }"</code>.
     *
     * @return expression value
     */
    public static ExpressionValue forValue(ASTExpression expression, boolean forceExpression)
    {
        return new ExpressionValue(expression, forceExpression);
    }


    /**
     * Internal method to convert an Object to an attribute value.
     *
     * @param value     object value
     *                 
     * @return expression value
     */
    static ExpressionValue toExpression(Object value)
    {
        return new ExpressionValue(ExpressionValueType.EXPRESSION, "{ " + generator.forValue(value) + " }", false);
    }


    /**
     * Reformats the expression string.
     *
     * @param expr  expression string
     *
     * @return formatted expression string
     */
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
            expr = expr.substring(0, closingBrace) + " }";
        }

        return expr;
    }
}

