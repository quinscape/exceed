package de.quinscape.exceed.runtime.model;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTComputedPropertyChain;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFloat;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.action.ActionCallGenerator;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.svenson.JSON;

import java.util.Map;
import java.util.Set;

/**
 * Renders transformed view model expressions.
 *
 *
 */
public class ClientExpressionRenderer
    extends ExpressionRenderer
{
    /**
     * Number of characters below which references to string props will be inlined into the expression instead of going
     * all the way over all the parents to the props.
     */
    public static final int STRING_INLINE_LIMIT = 120;

    private final ComponentModel componentModel;

    private final String componentId;

    private final ComponentPath path;

    private final ComponentDescriptor componentDescriptor;

    private final JSON generator = JSON.defaultJSON();

    private final String attrName;

    private final RuntimeApplication application;

    private final boolean isActionExpression;

    private final Map<String, ActionCallGenerator> actionCallGenerators;


    public ClientExpressionRenderer(RuntimeApplication application, ComponentModel componentModel, String attrName, ComponentPath path, Map<String,ActionCallGenerator> actionCallGenerators, boolean isActionExpression)


    {
        this.application = application;
        this.actionCallGenerators = actionCallGenerators;
        this.isActionExpression = isActionExpression;

        if (componentModel == null)
        {
            throw new IllegalArgumentException("componentModel can't be null");
        }

        if (path == null)
        {
            throw new IllegalArgumentException("path can't be null");
        }

        AttributeValue attr = componentModel.getAttribute("id");

        this.componentId = attr != null ? (String) attr.getValue() : null;
        this.path = path;
        this.componentModel = componentModel;
        this.attrName = attrName;

        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        this.componentDescriptor = componentRegistration != null ? componentRegistration.getDescriptor() : null;
    }


    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        String name = node.getName();

        boolean isModel = name.equals("model");
        boolean isProps = name.equals("props");

        String modelPath = null;

        if (isModel || isProps)
        {
            modelPath = path.modelPath();
        }

        StringBuilder buf = getBuffer();
        if (isModel)
        {
            buf.append(modelPath);
            return data;
        }
        else if(isProps)
        {
            buf.append(modelPath).append(".attrs");
            return data;
        }

        if (name.equals("vars"))
        {
            if (componentDescriptor == null || componentDescriptor.getVars() == null)
            {
                throw new InvalidClientExpressionException(componentModel.getName() + " has no vars");
            }

            buf.append("_v.data[");
            buf.append(generator.quote(componentId));
            buf.append("].vars");
            return true;
        }
        super.visit(node, data);
        return false;
    }



    private boolean renderInlinedConstant(AttributeValue attribute)
    {
        StringBuilder buf = getBuffer();
        AttributeValueType type = attribute.getType();
        if (type == AttributeValueType.STRING)
        {
            String str = (String) attribute.getValue();
            // only inline shortish strings
            if (str.length() < STRING_INLINE_LIMIT)
            {
                buf.append(generator.quote(str));
                return true;
            }
        }
        else if (type == AttributeValueType.EXPRESSION)
        {
            ASTExpression astExpression = attribute.getAstExpression();
            if (astExpression.jjtGetNumChildren() == 1)
            {
                Node firstNode = astExpression.jjtGetChild(0);
                if (firstNode instanceof ASTInteger)
                {
                    buf.append(((ASTInteger) firstNode).getValue());
                    return true;
                }
                else if (firstNode instanceof ASTFloat)
                {
                    buf.append(((ASTFloat) firstNode).getValue());
                    return true;
                }
                else if (firstNode instanceof ASTBool)
                {
                    buf.append(((ASTBool) firstNode).getValue());
                    return true;
                }
                else if (firstNode instanceof ASTString)
                {
                    buf.append(generator.quote(((ASTString) firstNode).getValue()));
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Object visit(ASTComputedPropertyChain node, Object data)
    {
        Node firstChild = node.jjtGetChild(0);
        if (firstChild instanceof ASTIdentifier)
        {
            ASTIdentifier ident = (ASTIdentifier) firstChild;
            if (ident.getName().equals("vars"))
            {
                if (componentDescriptor == null || componentDescriptor.getVars() == null)
                {
                    throw new InvalidClientExpressionException(componentModel.getName() + " has no vars");
                }

                Node second = node.jjtGetChild(1);
                if (second instanceof ASTString)
                {
                    String varName = ((ASTString) second).getValue();
                    if (!componentDescriptor.getVars().containsKey(varName))
                    {
                        throw new InvalidClientExpressionException(componentModel.getName() + " has no var '" + varName + "'");
                    }
                }
            }
            else if (ident.getName().equals("props"))
            {
                Node second = node.jjtGetChild(1);
                if (second instanceof ASTString)
                {
                    AttributeValue attribute = componentModel.getAttribute(((ASTString) second).getValue());
                    if (attribute != null)
                    {
                        if (renderInlinedConstant(attribute))
                        {
                            return data;
                        }
                    }
                }
            }
            return super.visit(node, data);
        }
        return super.visit(node, data);
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        StringBuilder buf = getBuffer();
        String operationName = node.getName();

        ActionCallGenerator gen;
        if (operationName.equals("param"))
        {
            buf.append("_v.param(");
            renderMultiBinary(node, ", ", data);
            buf.append(')');
            return data;
        }
        else if (operationName.equals("uri"))
        {
            buf.append("_v.uri( '/app/' + _sys.appName + ");
            renderMultiBinary(node, ", ", data);
            buf.append(')');
            return data;
        }
        else if ((gen = actionCallGenerators.get(operationName)) != null)
        {
            actionProlog();
            gen.renderJsCode(this, node);
            actionEpilog();
            return data;
        }

        return super.visit(node, data);
    }




    @Override
    public Object visit(ASTPropertyChain node, Object data)
    {
        Node firstChild = node.jjtGetChild(0);
        if (firstChild instanceof ASTIdentifier)
        {
            ASTIdentifier identifier = (ASTIdentifier) firstChild;
            if (identifier.getName().equals("vars"))
            {
                if (componentDescriptor == null || componentDescriptor.getVars() == null)
                {
                    throw new InvalidClientExpressionException(componentModel.getName() + " has no vars");
                }

                Node second = node.jjtGetChild(1);
                if (second instanceof ASTIdentifier)
                {
                    String varName = ((ASTIdentifier) second).getName();
                    if (!componentDescriptor.getVars().containsKey(varName))
                    {
                        throw new InvalidClientExpressionException(componentModel.getName() + " has no var '" + varName + "'");
                    }
                }
            }
            else if (identifier.getName().equals("props"))
            {
                Node second = node.jjtGetChild(1);
                if (second instanceof ASTIdentifier)
                {
                    AttributeValue attribute = componentModel.getAttribute(((ASTIdentifier) second).getName());
                    if (attribute != null)
                    {
                        if (renderInlinedConstant(attribute))
                        {
                            return data;
                        }
                    }
                }
            }
            return super.visit(node, data);
        }
        else if (firstChild instanceof ASTFunction)
        {
            if (isActionExpression)
            {

                int i;
                for (i = 0; i < node.jjtGetNumChildren(); i++)
                {
                    Node kid = node.jjtGetChild(i);

                    if (kid instanceof ASTFunction)
                    {
                        String opName = ((ASTFunction) kid).getName();
                        if (!actionCallGenerators.containsKey(opName))
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }

                // no actions: default handling
                if (i == 0)
                {
                    return super.visit(node, data);
                }

                // are all chain members are actions?
                if (i == node.jjtGetNumChildren())
                {
                    actionProlog();

                    for (i = 0; i < node.jjtGetNumChildren(); i++)
                    {
                        ASTFunction kid = (ASTFunction) node.jjtGetChild(i);

                        String opName = ((ASTFunction) kid).getName();

                        if (i > 0)
                        {
                            getBuffer().append(".then(function(){ return (");
                        }

                        actionCallGenerators.get(opName).renderJsCode(this, kid);

                        if (i > 0)
                        {
                            getBuffer().append(")})");
                        }
                    }
                    actionEpilog();
                }
                else
                {
                    throw new InvalidClientExpressionException("Property chain member " + node.jjtGetChild(i) + " is not a supported action generator: " + ExpressionRenderer.render(node));
                }
            }
            else
            {
                return super.visit(node, data);
            }
        }
        return data;
    }


    private void actionProlog()
    {
        getBuffer().append("function(){ return _v.observe(");
    }


    private void actionEpilog()
    {
        getBuffer().append(")}");
    }
}
