package de.quinscape.exceed.runtime.model;

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
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.context.ScopeDeclaration;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.service.ActionExpressionRenderer;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

/**
 * Renders transformed view model expressions.
 */
public class ViewExpressionRenderer
    extends ActionExpressionBaseRenderer
{
    private final static Logger log = LoggerFactory.getLogger(ViewExpressionRenderer.class);

    /**
     * Number of characters below which references to string props will be inlined into the expression instead of going
     * all the way over all the parents to the props.
     */
    public static final int STRING_INLINE_LIMIT = 120;

    private final ComponentModel componentModel;

    private final String componentId;

    private final ComponentRenderPath path;

    private final ComponentDescriptor componentDescriptor;

    private final JSON generator = SingleQuoteJSONGenerator.INSTANCE;

    private final String attrName;

    private final ApplicationModel applicationModel;

    private final View view;

    private final ScopeDeclarations declarations;


    public ViewExpressionRenderer(ApplicationModel applicationModel, View view, ComponentModel componentModel, String
        attrName, ComponentRenderPath path, ActionExpressionRenderer actionExpressionRenderer)
    {
        super(view, actionExpressionRenderer);

        if (applicationModel == null)
        {
            throw new IllegalArgumentException("applicationModel can't be null");
        }


        this.applicationModel = applicationModel;
        this.view = view;

        if (componentModel == null)
        {
            throw new IllegalArgumentException("componentModel can't be null");
        }

        if (path == null)
        {
            throw new IllegalArgumentException("path can't be null");
        }
        AttributeValue attr = componentModel.getAttribute(ComponentModel.ID_ATTRIBUTE);
        this.componentId = attr != null ? attr.getValue() : null;
        this.path = path;
        this.componentModel = componentModel;
        this.attrName = attrName;

        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        this.componentDescriptor = componentRegistration != null ? componentRegistration.getDescriptor() : null;

        declarations = applicationModel.getMetaData().getScopeMetaModel().lookup(view);
    }


    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        final String name = node.getName();

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
        else if (isProps)
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
            return data;
        }
        else
        {
            final PropDeclaration propDeclaration = componentDescriptor != null ? componentDescriptor.getPropTypes().get(attrName) : null;

            final ScopeDeclaration definition = declarations.get(name);

            if (definition != null)
            {
                final boolean isCursorExpression = propDeclaration != null && propDeclaration.getType() == PropType.CURSOR_EXPRESSION;
                if (isCursorExpression)
                {
                    buf.append("_v.scopeCursor(");
                }
                else
                {
                    buf.append("_v.scope(");
                }
                buf.append(generator.quote(name)).append(")");
                return data;
            }

        }

        super.visit(node, data);
        return data;
    }


    private boolean renderInlinedConstant(AttributeValue attribute)
    {
        StringBuilder buf = getBuffer();
        AttributeValueType type = attribute.getType();
        if (type == AttributeValueType.STRING)
        {
            String str = attribute.getValue();
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
                ensureModelContext();

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
                        throw new InvalidClientExpressionException(componentModel.getName() + " has no var '" +
                            varName + "'");
                    }
                }
                return super.visit(node, data);
            }
            else if (ident.getName().equals("props"))
            {
                ensureModelContext();

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


    private void ensureModelContext()
    {
        if (componentModel == null)
        {
            throw new IllegalStateException("Expression not in a component model context");
        }
    }


    @Override
    protected boolean handleLocalFunctions(String operationName, ASTFunction node)
    {
        StringBuilder buf = getBuffer();
        if (operationName.equals("param"))
        {
            buf.append("_v.param(");
            renderMultiBinary(node, ", ", null);
            buf.append(')');
            return true;
        }
        else if (operationName.equals("uri"))
        {
            buf.append("_v.uri( '/app/' + _sys.appName + ");
            renderMultiBinary(node, ", ", null);
            buf.append(')');
            return true;
        }
        else if (operationName.equals("transition"))
        {
            buf.append("_v.transition(");
            renderMultiBinary(node, ", ", null);
            buf.append(")");
            return true;
        }
        return false;
    }


    @Override
    protected boolean handleLocalIdentifiers(ASTPropertyChain node, ASTIdentifier identifier)
    {
        final String identifierName = identifier.getName();
        if (identifierName.equals("vars"))
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
                    throw new InvalidClientExpressionException(componentModel.getName() + " has no var '" + varName +
                        "'");
                }
                return false;
            }
        }
        else if (identifierName.equals("props"))
        {
            Node second = node.jjtGetChild(1);
            if (second instanceof ASTIdentifier)
            {
                AttributeValue attribute = componentModel.getAttribute(((ASTIdentifier) second).getName());
                if (attribute != null)
                {
                    if (renderInlinedConstant(attribute))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
