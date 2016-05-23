package de.quinscape.exceed.runtime.model;

import com.google.common.base.Objects;
import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.component.PropType;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ExpressionParserConstants;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.action.ClientActionRenderer;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.ActionExpressionRenderer;
import de.quinscape.exceed.runtime.service.ActionExpressionRendererFactory;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.SyslogCallGenerator;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.svenson.JSON;
import org.svenson.util.JSONBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Produces a special augmented view model JSON from a view model.
 *
 * The JSON contains components with an additional "exprs" property that contains the attribute expressions transformed
 * for client consumption.
 *
 * @see ViewExpressionRenderer
 */
public class ClientViewJSONGenerator
{
    private final static JSON json = JSON.defaultJSON();

    public static final String CONTEXT_DEFAULT_NAME = "context";

    public static final String MODEL_ATTR_NAME = "model";

    private final ActionExpressionRendererFactory actionExpressionRendererFactory;

    private Map<String, ClientActionRenderer> generators;


    public ClientViewJSONGenerator(ActionExpressionRendererFactory actionExpressionRendererFactory)
    {
        this.actionExpressionRendererFactory = actionExpressionRendererFactory;

        generators = new HashMap<>();
        generators.put("syslog", new SyslogCallGenerator());
    }

    public String toJSON(RuntimeApplication application, View view, JSONFormat jsonFormat)
    {
        JSONBuilder builder = JSONBuilder.buildObject();
        {
            builder.property("type", "view.View")
                .property("name", view.getName())
                .property("preview", view.isPreview())
                .property("processName", view.getProcessName());

            String version = view.getVersion();
            if (version != null)
            {
                builder.property("version", version);
            }

            JSONBuilder.Level lvl = builder.getCurrentLevel();

            builder.objectProperty("root");
            {
                dumpComponentRecursive(builder, application, view, view.getRoot(), new ComponentPath(), jsonFormat);
                builder.closeUntil(lvl);
            }
            builder.property("comments", view.getComments());
        }
        return builder.output();
    }



    private void dumpComponentRecursive(JSONBuilder builder, RuntimeApplication application, View view, ComponentModel componentModel, ComponentPath path, JSONFormat jsonFormat)
    {
        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        if (jsonFormat == JSONFormat.CLIENT && componentRegistration != null)
        {

            String providedContext = componentRegistration.getDescriptor().getProvidesContext();
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
        Set<String> attrNames = attrs == null ? Collections.emptySet() : attrs.getNames();

        Set<String> defaultAttrs = Collections.emptySet();

        if (componentRegistration != null)
        {
            Map<String, PropDeclaration> propTypes = componentRegistration.getDescriptor().getPropTypes();
            defaultAttrs = propTypes.keySet()
                .stream()
                .filter(name ->
                    !attrNames.contains(name) &&
                    propTypes.get(name).getDefaultValue() != null
                )
                .collect(Collectors.toSet());
        }

        if (attrs != null || defaultAttrs.size() > 0)
        {
            builder.objectProperty("attrs");
            for (String attrName : attrNames)
            {
                Object value = attrs.getAttribute(attrName).getValue();
                builder.property(attrName, value);
            }

            if (componentRegistration != null)
            {
                addDefaults(builder, view, componentRegistration.getDescriptor(), defaultAttrs, false, application,
                    componentModel, path);
            }

            builder.close();
        }

        if (jsonFormat == JSONFormat.CLIENT)
        {
            builder.objectProperty("exprs");
            if (attrs != null)
            {
                for (String attrName : attrNames)
                {
                    AttributeValue attribute = attrs.getAttribute(attrName);
                    ASTExpression expression = attribute.getAstExpression();

                    // find prop declaration for prop if such we have a registration / descriptor
                    PropDeclaration propDecl = null;
                    if (componentRegistration != null)
                    {
                        propDecl = componentRegistration.getDescriptor().getPropTypes().get(attrName);
                    }

                    if (expression != null)
                    {
                        /** if this is a context expression, we will deal with it later in {@link #provideContextExpressions(JSONBuilder, RuntimeApplication, View, ComponentModel, ComponentPath, ComponentDescriptor)} ) */
                        boolean isContextExpression = propDecl != null && propDecl.getContext() != null;
                        if (!isContextExpression)
                        {
                            builder.property(attrName, transformExpression(expression, application, view, componentModel, attrName, path, propDecl != null && propDecl.getType() == PropType.ACTION_EXPRESSION));
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

                addDefaults(builder, view, componentRegistration.getDescriptor(), defaultAttrs, true, application, componentModel, path);

                provideContextExpressions(builder, application, view, componentModel, path, descriptor);

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
                        builder.objectProperty("exprs");
                        builder.property("value", transformExpression(astExpression, application, view, componentModel, "value", path, false));
                        builder.close();

                    }
                    builder.close();
                }
                else
                {
                    builder.objectElement();
                    dumpComponentRecursive(builder, application, view, kid, kidPath, jsonFormat);
                }
                kidPath.increment();
            }
            builder.close();
        }
        builder.close();
    }


    private void addDefaults(JSONBuilder builder, View view, ComponentDescriptor descriptor, Set<String> defaultAttrs, boolean isExpression, RuntimeApplication application, ComponentModel componentModel, ComponentPath path)

    {
        Map<String, PropDeclaration> propTypes = descriptor.getPropTypes();
        for (String name : defaultAttrs)
        {
            PropDeclaration propDecl = propTypes.get(name);
            AttributeValue defaultValue = propDecl.getDefaultValue();
            if ((defaultValue.getAstExpression() != null) == isExpression)
            {
                if (!isExpression)
                {
                    if (defaultValue.getType() == AttributeValueType.EXPRESSION_ERROR)
                    {
                        builder.property(name, "[ERROR:" + defaultValue.getExpressionError() + "]");
                    }
                    else
                    {
                        builder.property(name, defaultValue.getValue());
                    }

                }
                else
                {
                    builder.property(name, transformExpression(defaultValue.getAstExpression(), application, view, componentModel, name, path, propDecl.getType() == PropType.ACTION_EXPRESSION));
                }
            }
        }
    }


    private void provideContextExpressions(JSONBuilder builder, RuntimeApplication application, View view, ComponentModel componentModel, ComponentPath path, ComponentDescriptor descriptor)
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
                            if (contextAttr.getType() == AttributeValueType.EXPRESSION_ERROR)
                            {
                                throw new InconsistentModelException("Context expression contains errors", contextAttr.getExpressionError());
                            }
                            contextName = contextAttr.getValue();
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
                        ComponentPath contextByType = findContextByType(path.getParent(), decl.getContextType());
                        if (contextByType == null)
                        {
                            if (decl.isRequired())
                            {
                                throw new IllegalStateException("Cannot find context for " + componentModel + " in " +
                                    "view '" + view.getName() + "'");
                            }
                            renameIdentifier(contextAST, "context", new ASTNull(ExpressionParserConstants.NULL));
                        }
                        else
                        {
                            contextName = contextByType.getContextName();
                            renameIdentifier(contextAST, "context", contextName);
                        }
                    }

                    // .. and transform it for client consumption
                    builder.property(propName, transformExpression(contextAST, application, view, componentModel, propName, path, false));
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


    private ComponentPath findContextByType(ComponentPath path, String type)
    {
        while (path != null)
        {
            String contextName = path.getContextName();
            if (contextName != null && (type == null || type.equals(path.getProvidedContext())))
            {
                return path;
            }
            path = path.getParent();
        }
        return null;
    }

    private String transformExpression(
        SimpleNode expression,
        RuntimeApplication application,
        View view, ComponentModel componentModel,
        String attrName,
        ComponentPath path,
        boolean isActionExpression)
    {
        ActionExpressionRenderer actionExpressionRenderer = isActionExpression ? actionExpressionRendererFactory.create(generators) : null;
        ActionExpressionBaseRenderer renderer = new ViewExpressionRenderer(application, view, componentModel, attrName,
            path, actionExpressionRenderer);

        if (isActionExpression)
        {
            expression = (SimpleNode) ExpressionUtil.handleAssignmentAction(expression);
        }

        expression.childrenAccept(renderer, null);
        if (isActionExpression)
        {
            return "function(){ return " +  renderer.getOutput() + "}";
        }
        else
        {
            return renderer.getOutput();
        }
    }

}

