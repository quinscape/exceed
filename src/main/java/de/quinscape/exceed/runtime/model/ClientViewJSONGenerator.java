package de.quinscape.exceed.runtime.model;

import com.google.common.base.Objects;
import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.svenson.util.JSONBuilder;

import java.util.List;
import java.util.Map;

/**
 * Produces a special augmented view model JSON from a view model.
 *
 * The JSON contains components with an additional "exprs" property that contains the attribute expressions transformed
 * for client consumption.
 *
 * @see ClientExpressionRenderer
 */
public class ClientViewJSONGenerator
{
    public static final String CONTEXT_DEFAULT_NAME = "context";

    public static final String MODEL_ATTR_NAME = "model";

    public String toJSON(RuntimeApplication application, View model, JSONFormat jsonFormat)
    {
        JSONBuilder builder = JSONBuilder.buildObject();
        {
            builder.property("type", "view.View")
                .property("name", model.getName())
                .property("preview", model.isPreview());

            String version = model.getVersion();
            if (version != null)
            {
                builder.property("version", version);
            }

            JSONBuilder.Level lvl = builder.getCurrentLevel();

            builder.objectProperty("root");
            {
                dumpComponentRecursive(builder, application, model.getRoot(), new ComponentPath(), jsonFormat);
                builder.closeUntil(lvl);
            }
            builder.property("comments", model.getComments());
        }
        return builder.output();
    }



    private void dumpComponentRecursive(JSONBuilder builder, RuntimeApplication application, ComponentModel componentModel, ComponentPath path, JSONFormat jsonFormat)
    {
        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        if (jsonFormat == JSONFormat.CLIENT && componentRegistration != null)
        {

            String providedContext = componentRegistration.getDescriptor().getProvidedContext();
            if (providedContext != null)
            {
                AttributeValue varAttr = componentModel.getAttribute("var");
                String contextName;
                if (varAttr == null)
                {
                    contextName = CONTEXT_DEFAULT_NAME;
                }
                else
                {
                    contextName = (String) varAttr.getValue();
                }
                path.setContextName(contextName);
                path.setProvidedContext(providedContext);
            }
        }

        builder.property("name", componentModel.getName());

        Attributes attrs = componentModel.getAttrs();

        if (attrs != null)
        {
            builder.objectProperty("attrs");
            for (String attrName : attrs.getNames())
            {
                Object value = attrs.getAttribute(attrName).getValue();
                builder.property(attrName, value);
            }

            builder.close();
        }

        if (jsonFormat == JSONFormat.CLIENT)
        {
            builder.objectProperty("exprs");
            if (attrs != null)
            {
                for (String attrName : attrs.getNames())
                {
                    AttributeValue attribute = attrs.getAttribute(attrName);
                    ASTExpression expression = attribute.getAstExpression();
                    if (expression != null)
                    {
                        PropDeclaration propDecl;
                        boolean isContextExpression = componentRegistration != null &&
                            (propDecl = componentRegistration.getDescriptor().getPropTypes().get(attrName)) != null &&
                            propDecl.getContext() != null;

                        if (!isContextExpression)
                        {
                            ClientExpressionRenderer visitor = new ClientExpressionRenderer(application, componentModel, attrName, path);
                            expression.childrenAccept(visitor, null);
                            String transformed = visitor.getOutput();
                            builder.property(attrName, transformed);
                        }
                    }
                }
            }

            if (componentRegistration != null)
            {
                ComponentDescriptor descriptor = componentRegistration.getDescriptor();

                if (descriptor.isModelAware())
                {
                    builder.property(MODEL_ATTR_NAME, path.modelPath());
                }

                provideContextExpressions(builder, application, componentModel, path, descriptor);

                builder.close();
            }
            else
            {
                builder.close();
            }
        }

        List<ComponentModel> kids = componentModel.getKids();
        if (kids != null)
        {
            builder.arrayProperty("kids");
            ComponentPath kidPath = path.firstChildPath();
            for (ComponentModel kid : kids)
            {
                if (kid.getName().equals(ComponentModel.STRING_MODEL_NAME))
                {
                    AttributeValue value = kid.getAttribute("value");
                    ASTExpression astExpression = value.getAstExpression();

                    builder.objectElement();
                    {
                        builder.property("name", ComponentModel.STRING_MODEL_NAME);

                        builder.objectProperty("attrs");
                        builder.property("value", value.getValue());
                        builder.close();
                    }

                    if (astExpression != null)
                    {
                        ClientExpressionRenderer renderer = new ClientExpressionRenderer(application, componentModel, "value", path);
                        astExpression.childrenAccept(renderer, null);

                        builder.objectProperty("exprs");
                        builder.property("value", renderer.getOutput());
                        builder.close();

                    }
                    builder.close();
                }
                else
                {
                    builder.objectElement();
                    dumpComponentRecursive(builder, application, kid, kidPath, jsonFormat);
                }
                kidPath.increment();
            }
            builder.close();
        }
        builder.close();
    }


    private void provideContextExpressions(JSONBuilder builder, RuntimeApplication application, ComponentModel componentModel, ComponentPath path, ComponentDescriptor descriptor)
    {
        // we check all property declarations of the current component
        Map<String, PropDeclaration> propTypes = descriptor.getPropTypes();
        for (Map.Entry<String, PropDeclaration> entry : propTypes.entrySet())
        {
            String propName = entry.getKey();
            PropDeclaration decl = entry.getValue();

            // when a property has a context expression (bools are "context"/null at this point)
            String contextExpr = decl.getContext();
            if (contextExpr != null)
            {
                // we parse the context expression renaming the "context" variable to our actual context
                // name
                try
                {
                    ASTExpression contextAST = ExpressionParser.parse(contextExpr);

                    // we look if the context attribute has been set
                    AttributeValue contextAttr = componentModel.getAttribute(propName);
                    String contextName;
                    if (contextAttr != null)
                    {
                        // .. and use that context name if set
                        ASTExpression contextAttrAST = contextAttr.getAstExpression();
                        if (contextAttrAST == null)
                        {
                            if (contextAttr.getType() != AttributeValueType.STRING)
                            {
                                throw new InconsistentModelException("Context attribute must be a string value containing" +
                                    " a context name: " + componentModel);
                            }

                            contextName = (String) contextAttr.getValue();
                            renameIdentifier(contextAST, "context", contextName);
                        }
                        else
                        {
                            renameIdentifier(contextAST, "context", contextAttrAST.jjtGetChild(0));
                        }
                    }
                    else
                    {
                        // otherwise we default to the first parent of matching type to provide a context
                        // type can be null here meaning to accept every kind of context
                        contextName = findContext(path.getParent(), decl.getContextType());
                        renameIdentifier(contextAST, "context", contextName);
                    }

                    // .. and transform it for client consumption
                    ClientExpressionRenderer renderer = new ClientExpressionRenderer(application, componentModel, propName, path);
                    contextAST.childrenAccept(renderer, null);
                    builder.property(propName, renderer.getOutput());
                }
                catch (ParseException e)
                {
                    throw new ExceedRuntimeException("Error parsing context expression for component " +
                        componentModel.getName() + ", prop '" + propName);
                }
            }
        }
    }


    private void renameIdentifier(ASTExpression node, String from, Object to)
    {
        if (!Objects.equal(from,to))
        {
            renameRecursive(node, from, to);
        }
    }

    private Node renameRecursive(Node node, String from, Object to)
    {
        if (node instanceof ASTIdentifier)
        {
            ASTIdentifier ident = (ASTIdentifier) node;
            if (Objects.equal(ident.getName(), from))
            {
                if (to instanceof String)
                {
                    ident.setName((String) to);
                }
                else
                {
                    return (Node) to;
                }
            }
            return null;
        }

        if (node != null)
        {
            for (int i=0; i < node.jjtGetNumChildren(); i++)
            {
                Node replacement = renameRecursive(node.jjtGetChild(i), from, to);
                if (replacement != null)
                {
                    node.jjtAddChild(replacement, i);
                }
            }
        }
        return null;
    }


    private String findContext(ComponentPath path, String type)
    {
        while (path != null)
        {
            String contextName = path.getContextName();
            if (contextName != null && (type == null || type.equals(path.getProvidedContext())))
            {
                return contextName;
            }
            path = path.getParent();
        }
        return null;
    }

}
