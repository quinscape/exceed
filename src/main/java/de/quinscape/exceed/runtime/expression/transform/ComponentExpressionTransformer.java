package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTDecimal;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.ASTPropertyChainSquare;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.model.ComponentRenderPath;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;
import org.svenson.JSON;


public class ComponentExpressionTransformer
    implements ExpressionTransformer
{
    public static final String PROPS_IDENTIFIER = "props";

    public static final String MODEL_IDENTIFIER = "model";

    public static final String VARS_IDENTIFIER = "vars";

    private static final int STRING_INLINE_LIMIT = 60;

    public static final String CONTEXT_IDENTIFIER = "context";

    public static final String DATA_CURSOR = "DataCursor";


    private final ComponentModel componentModel;

    private final ComponentRenderPath path;

    private final String attrName;

    private final ComponentRenderPath contextParent;

    private final PropDeclaration propDecl;


    public ComponentExpressionTransformer(
        ComponentModel componentModel, ComponentRenderPath path, String attrName,
        ComponentRenderPath contextParent,
        PropDeclaration propDecl
    )
    {
        this.componentModel = componentModel;
        this.path = path;
        this.attrName = attrName;

        this.contextParent = contextParent;
        this.propDecl = propDecl;
    }


    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        final ASTIdentifier ident = getIdentifier(node);
        if (ident == null)
        {
            return false;
        }

        final String name = ident.getName();
        return name.equals(PROPS_IDENTIFIER) || name.equals(MODEL_IDENTIFIER) || name.equals(VARS_IDENTIFIER) || name.equals(CONTEXT_IDENTIFIER);
    }


    private ASTIdentifier getIdentifier(Node node)
    {
        if (node instanceof ASTPropertyChain)
        {
            final Node kid = node.jjtGetChild(0);
            if (kid instanceof ASTIdentifier)
            {
                return (ASTIdentifier) kid;
            }
        }
        else if (node instanceof ASTIdentifier)
        {
            final Node parent = node.jjtGetParent();
            if (parent instanceof ASTPropertyChainDot || parent instanceof ASTPropertyChainSquare)
            {
                // we ignore identifiers that are secondary links in property chains
                return null;
            }

            return (ASTIdentifier) node;
        }
        return null;
    }



    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        final ASTIdentifier ident = getIdentifier(node);
        final ComponentDescriptor componentDescriptor= componentModel.getComponentRegistration() != null ? componentModel.getComponentRegistration().getDescriptor() : null;

        final SingleQuoteJSONGenerator generator = SingleQuoteJSONGenerator.INSTANCE;

        final boolean isStringExpression = componentModel.getName().equals(ComponentModel.STRING_MODEL_NAME) && attrName.equals("value");
        if (node instanceof ASTPropertyChain)
        {
            final String startIdentifier = ident.getName();
            if (startIdentifier.equals(PROPS_IDENTIFIER))
            {
                // we're in a chain, so we at least 2 valid indizes
                final Node second = node.jjtGetChild(1).jjtGetChild(0);
                if (second instanceof ASTIdentifier)
                {
                    ExpressionValue attribute = componentModel.getAttribute(((ASTIdentifier) second).getName());
                    if (attribute != null)
                    {
                        if (renderInlinedConstant(ctx, attribute))
                        {
                            return;
                        }
                    }
                }
            }
            else if (startIdentifier.equals(CONTEXT_IDENTIFIER))
            {
                // -> "context.xxx" as part of a chain
                if (contextParent == null)
                {
                    if (isStringExpression)
                    {
                        ctx.output("'null'");
                    }
                    else
                    {
                        ctx.output("null");
                    }
                }
                else
                {
                    if (isStringExpression)
                    {
                        ctx.output("JSON.stringify(");
                    }
                    final String contextName = contextParent.getContextName();
                    ctx.output(contextName);
                    ctx.output(".getCursor([");

                    final int numChildren = node.jjtGetNumChildren();
                    for (int i=1; i < numChildren; i++)
                    {
                        if (i > 1)
                        {
                            ctx.output(",");
                        }

                        final Node link = node.jjtGetChild(i);

                        if (link instanceof ASTPropertyChainDot)
                        {
                            final Node kid = link.jjtGetChild(0);

                            if (kid instanceof ASTIdentifier)
                            {
                                ctx.output(generator.quote(((ASTIdentifier) kid).getName()));
                            }
                            else
                            {
                                throw new InvalidExpressionException("Invalid cursor expression: " + ExpressionUtil.renderExpressionOf(kid));
                            }
                        }
                        else
                        {
                            ctx.applyRecursive(link.jjtGetChild(0));
                        }
                    }

                    ctx.output("])");
                    if (isStringExpression)
                    {
                        ctx.output(".get())");
                    }
                }
                return;
            }
        }
        else if (node instanceof ASTIdentifier)
        {
            final String identifierName = ((ASTIdentifier) node).getName();

            boolean isModel = identifierName.equals(MODEL_IDENTIFIER);
            boolean isProps = identifierName.equals(PROPS_IDENTIFIER);

            String modelPath = null;

            if (isModel || isProps)
            {
                modelPath = path.modelPath();
            }

            if (isModel)
            {
                ctx.output(modelPath);
                return;
            }
            else if (isProps)
            {
                ctx.output(modelPath);
                ctx.output(".attrs");
                return;
            }

            if (identifierName.equals(VARS_IDENTIFIER))
            {
                if (componentDescriptor == null || componentDescriptor.getVars() == null)
                {
                    throw new InvalidClientExpressionException(componentModel.getName() + " has no vars");
                }
                ctx.output("_v.data[");
                ctx.output(generator.quote(componentModel.getComponentId()));
                ctx.output("].vars");
                return;
            }
            else if (identifierName.equals(CONTEXT_IDENTIFIER))
            {
                if (contextParent == null)
                {
                    if (isStringExpression)
                    {
                        ctx.output("'null'");
                    }
                    else
                    {
                        ctx.output("null");
                    }
                }
                else
                {
                    final String contextName = contextParent.getContextName();
                    
                    if (isStringExpression)
                    {
                        ctx.output("JSON.stringify(");
                        ctx.output(contextName);
                        ctx.output(".get())");
                    }
                    else
                    {
                        ctx.output(contextName);
                    }
                }
                return;
            }
        }
        ctx.renderDefault();
    }




    private boolean renderInlinedConstant(ExpressionTransformationContext ctx, ExpressionValue attribute)
    {
        final JSON generator = SingleQuoteJSONGenerator.INSTANCE;

        ExpressionValueType type = attribute.getType();
        if (type == ExpressionValueType.STRING)
        {
            String str = attribute.getValue();
            // only inline shortish strings
            if (str.length() < STRING_INLINE_LIMIT)
            {
                ctx.output(generator.quote(str));
                return true;
            }
        }
        else if (type == ExpressionValueType.EXPRESSION)
        {
            ASTExpression astExpression = attribute.getAstExpression();
            if (astExpression.jjtGetNumChildren() == 1)
            {
                Node firstNode = astExpression.jjtGetChild(0);
                if (firstNode instanceof ASTInteger)
                {
                    ctx.output(((ASTInteger) firstNode).getValue());
                    return true;
                }
                else if (firstNode instanceof ASTDecimal)
                {
                    ctx.output(((ASTDecimal) firstNode).getValue());
                    return true;
                }
                else if (firstNode instanceof ASTBool)
                {
                    ctx.output(((ASTBool) firstNode).getValue());
                    return true;
                }
                else if (firstNode instanceof ASTString)
                {
                    ctx.output(generator.quote(((ASTString) firstNode).getValue()));
                    return true;
                }
            }
        }
        return false;
    }
}
