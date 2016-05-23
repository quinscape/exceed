package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.runtime.action.ClientActionRenderer;
import de.quinscape.exceed.runtime.model.ActionExpressionBaseRenderer;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

import java.util.Map;

/**
 * Renders expression language action expression into JavaScript promise chains for client consumption
 */
public class ActionExpressionRenderer
{
    private final Map<String, ClientActionRenderer> actionCallGenerators;


    public ActionExpressionRenderer(Map<String, ClientActionRenderer> actionCallGenerators)
    {
        this.actionCallGenerators = actionCallGenerators;
    }

    public boolean hasOperation(String operationName)
    {
        return actionCallGenerators.containsKey(operationName);
    }

    public void render(String operationName, ASTFunction node, ActionExpressionBaseRenderer baseRenderer)
    {
        ClientActionRenderer gen = actionCallGenerators.get(operationName);
        actionProlog(baseRenderer);
        gen.renderJsCode(baseRenderer, node);
        actionEpilog(baseRenderer);
    }

    private void actionProlog(ActionExpressionBaseRenderer baseRenderer)
    {
        StringBuilder buf = baseRenderer.getBuffer();
        buf.append("_a.observe(");
    }


    private void actionEpilog(ExpressionRenderer baseRenderer)
    {
        baseRenderer.getBuffer().append(")");
    }


    public void renderChain(ASTExpressionSequence node, ActionExpressionBaseRenderer baseRenderer)
    {
        actionProlog(baseRenderer);

        StringBuilder buffer = baseRenderer.getBuffer();

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            ASTFunction kid = (ASTFunction) node.jjtGetChild(i);

            String opName = ((ASTFunction) kid).getName();

            if (i > 0)
            {
                buffer.append(".then(function(){ return (");
            }

            actionCallGenerators.get(opName).renderJsCode(baseRenderer, kid);

            if (i > 0)
            {
                buffer.append(")})");
            }
        }
        actionEpilog(baseRenderer);
    }
}
