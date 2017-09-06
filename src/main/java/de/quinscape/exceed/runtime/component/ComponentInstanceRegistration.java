package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParserTreeConstants;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Per instance registration for a component model contains the component registration and the per-component prepared
 * and component var defaults.
 * <p>
 *     The var defaults are a bit awkward in that they are server-side produced values that might contain props references
 *     to their components or context variable accesses. The component props however are part of the client-side code that
 *     does not needs to get transpiled to nashorn js to be executed.
 * </p>
 * <p>
 *     To solve this, we store the vars expressions in the per-component registration with inlined props references. Then
 *     we can transpile these action defaults to nashorn later with the correct context definitions for the component's view.
 * </p>
 */
public class ComponentInstanceRegistration
{
    private final ComponentRegistration componentRegistration;

    private final Map<String, ASTExpression> varExpressions;


    public ComponentInstanceRegistration(
        ComponentRegistration componentRegistration,
        ComponentModel componentModel
    )
    {
        this.componentRegistration = componentRegistration;


        final Map<String, String> vars = componentRegistration.getDescriptor().getVars();
        if (vars == null || vars.size() == 0)
        {
            varExpressions = Collections.emptyMap();
        }
        else
        {
            varExpressions = new HashMap<>();


            for (Map.Entry<String, String> e : vars.entrySet())
            {
                final String varName = e.getKey();
                final ExpressionValue value = ExpressionValue.forValue(e.getValue(), true);

                final ASTExpression astExpression = value.getAstExpression();
                if (astExpression != null)
                {
                    inlineProps(varName, componentRegistration, astExpression, componentModel);
                    varExpressions.put(varName, astExpression);
                }

            }
        }
    }

    static void inlineProps(
        String varName, ComponentRegistration registration, Node node,
        ComponentModel componentModel
    )
    {
        final int numChildren = node.jjtGetNumChildren();
        if (node instanceof ASTPropertyChain)
        {
            final Node first = node.jjtGetChild(0);
            if (first instanceof ASTIdentifier && ((ASTIdentifier) first).getName().equals("props"))
            {
                final String propName = ExpressionUtil.getNameOrValue(node.jjtGetChild(1).jjtGetChild(0));
                Node propsAST = getComponentPropExpression(registration, componentModel, propName);

                // if the props chain has more than 2 links
                if (node.jjtGetNumChildren() > 2)
                {
                    // we create a new ASTPropertyChain link with the inlined value as first like, followed by the
                    // remaining links
                    
                    ASTPropertyChain newChain = new ASTPropertyChain(ExpressionParserTreeConstants.JJTPROPERTYCHAIN);

                    for (int i = numChildren - 1; i >= 2; i--)
                    {
                        // we move the chain links directly to the new chain
                        final Node chainLink = node.jjtGetChild(i);
                        newChain.jjtAddChild(chainLink, i - 1);
                        chainLink.jjtSetParent(newChain);
                    }

                    newChain.jjtAddChild(propsAST, 0);
                    propsAST.jjtSetParent(newChain);

                    // replace old with new chain
                    ExpressionUtil.replaceNode(node, newChain);

                    if (propsAST instanceof ASTNull)
                    {
                        throw new InvalidExpressionException("Error inlining props for var default '" + varName + " for component '" + componentModel + ": Cannot walk undefined property value: " + ExpressionUtil.renderExpressionOf(newChain));
                    }
                }
                else
                {
                    // replace props chain with inline value
                    ExpressionUtil.replaceNode(node, propsAST);
                }
                return;
            }
        }
        for (int i = 0; i < numChildren ; i++)
        {
            inlineProps(varName, registration, node.jjtGetChild(i), componentModel);
        }
    }


    private static Node getComponentPropExpression(ComponentRegistration registration, ComponentModel componentModel, String propName)
    {
        final ExpressionValue value = componentModel.getAttribute(propName);

        Node result;
        if (value == null)
        {
            final PropDeclaration propDecl = registration.getDescriptor().getPropTypes().get(propName);
            if (propDecl != null && propDecl.getDefaultValue() != null)
            {
                result = propDecl.getDefaultValue().getAstExpression();
            }
            else
            {
                result = new ASTNull(ExpressionParserTreeConstants.JJTNULL);
            }
        }
        else
        {
            final ASTExpression exprFromProp = value.getAstExpression();
            if (exprFromProp != null)
            {
                result = exprFromProp;
            }
            else
            {
                final ASTString strNode = new ASTString(ExpressionParserTreeConstants.JJTSTRING);
                strNode.setValue(value.getValue());
                result = strNode;
            }
        }

        if (result instanceof ASTExpression)
        {
            result = result.jjtGetChild(0);
        }

        return result;
    }


    public Map<String, ExpressionValue> getQueryExpressions(RuntimeContext runtimeContext)
    {
        return componentRegistration.getQueryExpressions(runtimeContext);
    }

    public ComponentDescriptor getDescriptor()
    {
        return componentRegistration.getDescriptor();
    }


    public String getComponentName()
    {
        return componentRegistration.getComponentName();
    }


    public String getStyles()
    {
        return componentRegistration.getStyles();
    }


    public DataProvider getDataProvider()
    {
        return componentRegistration.getDataProvider();
    }


    public String getModuleName()
    {
        return componentRegistration.getModuleName();
    }


    public QueryTransformer getQueryTransformer()
    {
        return componentRegistration.getQueryTransformer();
    }


    public Map<String, ASTExpression> getVarExpressions()
    {
        return varExpressions;
    }

}
