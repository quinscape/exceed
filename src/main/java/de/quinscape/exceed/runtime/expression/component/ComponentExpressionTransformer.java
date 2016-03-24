package de.quinscape.exceed.runtime.expression.component;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;

public class ComponentExpressionTransformer
{
    private final boolean actionExpression;

    private final ActionService actionService;

    private final ExpressionService expressionService;

    public ComponentExpressionTransformer(ExpressionService expressionService, ActionService actionService, boolean actionExpression)
    {
        this.actionExpression = actionExpression;
        this.actionService = actionService;
        this.expressionService = expressionService;

    }
    public String transformExpression(String s) throws ParseException
    {
        ASTExpression astExpression = ExpressionParser.parse(s);
        ComponentExpressionEnvironment env = new ComponentExpressionEnvironment(actionService, actionExpression);

        expressionService.evaluate(astExpression, env);

        return env.getJavaScriptExpression();
    }


}
