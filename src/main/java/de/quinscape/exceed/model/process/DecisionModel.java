package de.quinscape.exceed.model.process;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.svenson.JSONProperty;

public class DecisionModel
    extends Model
{
    private String expression;
    private Transition transition;

    private ASTExpression expressionAST;


    public String getExpression()
    {
        return expression;
    }


    public void setExpression(String expression) throws ParseException
    {
        this.expression = expression;
        this.expressionAST = ExpressionParser.parse(expression);
    }


    @JSONProperty(ignore = true)
    public ASTExpression getExpressionAST()
    {
        return expressionAST;
    }

    public Transition getTransition()
    {
        return transition;
    }


    public void setTransition(Transition transition)
    {
        this.transition = transition;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "expression = '" + expression + '\''
            + ", transition = " + transition
            ;
    }
}

