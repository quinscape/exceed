package de.quinscape.exceed.runtime.expression.component;

import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFloat;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.controller.ActionNotFoundException;
import de.quinscape.exceed.runtime.controller.ActionRegistry;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironmentException;
import de.quinscape.exceed.runtime.expression.Operation;
import org.svenson.JSON;

public class ComponentExpressionTransformer
{
    private final boolean actionExpression;

    private final ActionRegistry actionRegistry;

    private JSON generator = new JSON('\'');


    public ComponentExpressionTransformer(boolean actionExpression, ActionRegistry actionRegistry)
    {
        this.actionExpression = actionExpression;
        this.actionRegistry = actionRegistry;

    }


    public String transformExpression(String s) throws ParseException
    {
        ASTExpression astExpression = ExpressionParser.parse(s);
        ComponentExpressionEnvironment env = new ComponentExpressionEnvironment();
        astExpression.jjtAccept(env, null);

        return env.getJavaScriptExpression();
    }


    public class ComponentExpressionEnvironment
        extends ExpressionEnvironment
    {
        private final StringBuilder output;


        public ComponentExpressionEnvironment()
        {

            comparatorsAllowed = true;
            arithmeticOperatorsAllowed = true;
            logicalOperatorsAllowed = true;
            complexLiteralsAllowed = true;

            output = new StringBuilder();
        }


        @Override
        protected Object resolveIdentifier(String name)
        {
            if (name.equals("context"))
            {
                output.append("context");
            }

            throw new ExpressionEnvironmentException("Unknown identifier '" + name);
        }


        @Operation
        public Object prop(ASTFunction astFunction)
        {
            output.append("props[");
            astFunction.jjtGetChild(0).jjtAccept(this, null);
            output.append(']');
            return null;
        }


        @Operation
        public Object var(ASTFunction astFunction)
        {
            output.append("vars[");
            astFunction.jjtGetChild(0).jjtAccept(this, null);
            output.append(']');
            return null;
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
        protected Object undefinedOperation(ASTFunction node, Object chainObject)
        {
            if (!actionExpression)
            {
                super.undefinedOperation(node, chainObject);
            }

            String actionName = node.getName();
            if (!actionRegistry.getActionNames().contains(actionName))
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
                key = ((ASTString)keyNode).getValue();
            }

            output
                .append(',')
                .append(generator.quote(key))
                .append(':');
            ValueNode.jjtAccept(this, null);

            return null;
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


        public String getJavaScriptExpression()
        {
            return output.toString();
        }
    }
}
