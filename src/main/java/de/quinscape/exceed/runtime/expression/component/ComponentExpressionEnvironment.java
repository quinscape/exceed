package de.quinscape.exceed.runtime.expression.component;

import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTFloat;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.controller.ActionNotFoundException;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironmentException;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;
import org.svenson.JSON;

public class ComponentExpressionEnvironment
    extends ExpressionEnvironment
{
    private final ActionService actionService;

    private final boolean actionExpression;

    private final StringBuilder output;

    private final static JSON generator = SingleQuoteJSONGenerator.INSTANCE;


    public ComponentExpressionEnvironment(ActionService actionService, boolean actionExpression)
    {
        this.actionService = actionService;
        this.actionExpression = actionExpression;
        output = new StringBuilder();

    }


    @Override
    public Object resolveIdentifier(String name)
    {
        if (name.equals("context"))
        {
            output.append("context");
        }

        throw new ExpressionEnvironmentException("Unknown identifier '" + name);
    }


    @Override
    public Object visit(ASTPropertyChain node, Object data)
    {
        if (actionExpression)
        {
            output.append('[');
        }
        super.visit(node, data);
        if (actionExpression)
        {
            output.append(']');
        }
        return null;
    }


    @Override
    protected Object propertyChainPart(Node kid, Object chainObject, int i)
    {
        if (i != 0)
        {
            output.append(',');
        }
        super.propertyChainPart(kid, chainObject, i);
        return null;
    }


    @Override
    public Object undefinedOperation(ExpressionContext<ExpressionEnvironment> ctx, ASTFunction node, Object chainObject)
    {
        if (!actionExpression)
        {
            super.undefinedOperation(ctx, node, chainObject);
        }

        String actionName = node.getName();
        if (!actionService.getActionNames().contains(actionName))
        {
            throw new ActionNotFoundException("Invalid action reference: " + actionName);
        }

        if (node.jjtGetNumChildren() > 0)
        {
            Node kid = node.jjtGetChild(0);
            if (!(kid instanceof ASTMap))
            {
                throw new ExpressionEnvironmentException(this.environmentName + ": Action parameter must be map literal");
            }
            else
            {
                output.append("{'action':").append(generator.quote(actionName));

                kid.jjtAccept(this, null);
                output.append('}');
            }
        }
        else
        {
            output.append("{'action':").append(generator.quote(actionName)).append('}');
        }
        return null;
    }


    @Override
    public Object visit(ASTMapEntry node, Object data)
    {
        Node keyNode = node.jjtGetChild(0);
        Node ValueNode = node.jjtGetChild(1);

        String key;
        if (keyNode instanceof ASTIdentifier)
        {
            key = ((ASTIdentifier) keyNode).getName();
        }
        else
        {
            key = ((ASTString) keyNode).getValue();
        }

        output
            .append(',')
            .append(generator.quote(key))
            .append(':');
        ValueNode.jjtAccept(this, null);

        return null;
    }


    @Override
    protected boolean logicalOperatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean comparatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean complexLiteralsAllowed()
    {
        return true;
    }


    @Override
    protected boolean arithmeticOperatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean expressionSequenceAllowed()
    {
        return true;
    }


    @Override
    public Object visit(ASTFloat node, Object data)
    {
        output.append(super.visit(node, data));
        return null;
    }


    @Override
    public Object visit(ASTInteger node, Object data)
    {
        output.append(String.valueOf(super.visit(node, data)));
        return null;
    }


    @Override
    public Object visit(ASTBool node, Object data)
    {
        output.append(super.visit(node, data));
        return null;
    }


    @Override
    public Object visit(ASTNull node, Object data)
    {
        output.append(super.visit(node, data));
        return null;
    }


    @Override
    public Object visit(ASTString node, Object data)
    {
        output.append(generator.quote(node.getValue()));
        return null;
    }


    public StringBuilder getOutput()
    {
        return output;
    }


    public String getJavaScriptExpression()
    {
        return output.toString();
    }
}
