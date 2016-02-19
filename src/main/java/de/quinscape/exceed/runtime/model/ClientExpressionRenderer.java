package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTComputedPropertyChain;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFloat;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.svenson.JSON;

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


    public ClientExpressionRenderer(ComponentModel componentModel, ComponentPath path)
    {

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
            if (identifier.getName().equals("props"))
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
        }
        return super.visit(node, data);
    }


    private boolean renderInlinedConstant(AttributeValue attribute)
    {
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
        }
        return super.visit(node, data);
    }


}
