package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.component.ComponentClasses;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.expression.transform.ActionExpressionTransformer;
import de.quinscape.exceed.runtime.expression.transform.ComponentExpressionTransformer;
import de.quinscape.exceed.runtime.expression.transform.DefaultExpressionTransformerFactory;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformer;
import de.quinscape.exceed.runtime.js.ExpressionBundle;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.JsExpressionRenderer;
import de.quinscape.exceed.runtime.js.ScriptBuffer;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.JSONBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;
import static de.quinscape.exceed.runtime.util.ExpressionUtil.*;

/**
 * Produces a special augmented view model JSON from a view model.
 *
 * The JSON contains components with an additional "exprs" property that contains the attribute expressions transformed
 * for client consumption.
 *
 */
public class ClientViewJSONGenerator
{

    private final static Logger log = LoggerFactory.getLogger(ClientViewJSONGenerator.class);

    public static final String CONTEXT_DEFAULT_NAME = "context";

    public static final String MODEL_ATTR_NAME = "model";

    private static final ComponentModel TITLE_MODEL = component("div").getComponent();

    private final JsExpressionRenderer renderer;

    private final ActionService actionService;

    public ClientViewJSONGenerator(ActionService actionService)
    {
        this.actionService = actionService;
        renderer = new JsExpressionRenderer(
            Collections.emptyList()
        );
    }

    public String toJSON(ApplicationModel applicationModel, View view, JSONFormat jsonFormat)
    {
        JSONBuilder builder = JSONBuilder.buildObject();

        ScriptBuffer scriptBuffer = new ScriptBuffer();

        builder.property("type", "xcd.view.View")
            .property("name", view.getName())
            .property("synthetic", view.isSynthetic())
            .property("processName", view.getProcessName());


        builder.propertyUnlessNull("identityGUID", view.getIdentityGUID());
        builder.propertyUnlessNull("versionGUID", view.getVersionGUID());


        final String title = view.getTitle();
        final ASTExpression titleExpression = view.getTitleExpression();

        builder.property("title", title);

        if (titleExpression != null)
        {
            builder.property("titleExpr", transformExpression(titleExpression, applicationModel, view, TITLE_MODEL, "title", new ComponentRenderPath("root"), ExpressionType.VALUE,
                scriptBuffer
            ));
        }
        else
        {
            builder.property("titleExpr", SingleQuoteJSONGenerator.INSTANCE.quote(title));
        }

        builder.objectProperty("content");

        for (Map.Entry<String, ComponentModel> entry : view.getContent().entrySet())
        {
            final String contentName = entry.getKey();
            final ComponentModel contentRoot = entry.getValue();

            JSONBuilder.Level lvl = builder.getCurrentLevel();
            builder.objectProperty(contentName);
            dumpComponentRecursive(builder, applicationModel, view, contentRoot, new ComponentRenderPath(contentName), jsonFormat,
                scriptBuffer
            );
            builder.closeUntil(lvl);
        }
        builder.close();

        StringBuilder constantsBuilder = new StringBuilder();
        ExpressionBundle.dumpResults(constantsBuilder, scriptBuffer, scriptBuffer.getPushed());

        builder.property("comments", view.getComments());
        builder.property("contextDependencies", view.getContextDependencies());
        builder.property("constants",  constantsBuilder.toString() );

        return builder.output();
    }



    private void dumpComponentRecursive(
        JSONBuilder builder, ApplicationModel application, View view, ComponentModel componentModel,
        ComponentRenderPath path, JSONFormat jsonFormat, ScriptBuffer scriptBuffer
    )
    {
        ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();
        String uniqueContextName = null;
        if (jsonFormat == JSONFormat.INTERNAL && componentRegistration != null)
        {
            String providedContext = componentRegistration.getDescriptor().getProvidesContext();
            if (providedContext != null)
            {
                ExpressionValue varAttr = componentModel.getAttribute(ComponentModel.VAR_ATTRIBUTE);
                if (varAttr == null)
                {
                    uniqueContextName = path.getUniqueName(CONTEXT_DEFAULT_NAME);
                }
                else
                {
                    uniqueContextName = path.getUniqueName(varAttr.getValue());
                }
            }
            path.setContextName(uniqueContextName);
            path.setProvidedContext(providedContext);
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

        if (attrs != null || defaultAttrs.size() > 0 || uniqueContextName != null)
        {
            builder.objectProperty("attrs");
            for (String attrName : attrNames)
            {
                Object value = attrs.getAttribute(attrName).getValue();

                if (!attrName.equals(ComponentModel.VAR_ATTRIBUTE))
                {
                    builder.property(attrName, value);
                }
            }

            if (componentRegistration != null)
            {
                addDefaults(builder, view, componentRegistration.getDescriptor(), defaultAttrs, false, application,
                    componentModel, path, scriptBuffer
                );
            }

            if (uniqueContextName != null)
            {
                builder.property("var", uniqueContextName);
            }
            builder.close();
        }

        if (jsonFormat == JSONFormat.INTERNAL)
        {
            builder.objectProperty("exprs");
            if (attrs != null)
            {
                for (String attrName : attrNames)
                {
                    ExpressionValue attribute = attrs.getAttribute(attrName);
                    ASTExpression expression = attribute.getAstExpression();

                    // find prop declaration for prop if such we have a registration / descriptor
                    PropDeclaration propDecl = null;
                    if (componentRegistration != null)
                    {
                        propDecl = componentRegistration.getDescriptor().getPropTypes().get(attrName);
                    }

                    final ExpressionType expressionType = getExpressionType(propDecl);

                    if (expression != null)
                    {
                        builder.property(
                            attrName,
                            transformExpression(
                                expression,
                                application,
                                view,
                                componentModel,
                                attrName,
                                path,
                                expressionType,
                                scriptBuffer
                            )
                        );
                    }
                }
            }

            if (componentRegistration != null)
            {
                ComponentDescriptor descriptor = componentRegistration.getDescriptor();

                if (descriptor.hasClass(ComponentClasses.MODEL_AWARE) || hasCursorProp(descriptor))
                {
                    builder.property(MODEL_ATTR_NAME, path.modelPath());
                }

                addDefaults(builder, view, componentRegistration.getDescriptor(), defaultAttrs, true, application, componentModel, path,
                    scriptBuffer
                );

                //provideContextExpressions(builder, application, view, componentModel, path, descriptor);

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
            ComponentRenderPath kidPath = null;
            for (ComponentModel kid : kids)
            {
                final String name = kid.getName();

                if (kidPath == null)
                {
                    kidPath = path.firstChildPath(name);
                }
                else
                {
                    kidPath.increment(name);
                }

                if (name.equals(ComponentModel.STRING_MODEL_NAME))
                {
                    ExpressionValue value = kid.getAttribute("value");
                    ASTExpression astExpression = value.getAstExpression();

                    builder.objectElement();

                    builder.property("name", ComponentModel.STRING_MODEL_NAME);

                    builder.objectProperty("attrs");
                    builder.property("value", value.getValue());
                    builder.close();

                    if (astExpression != null)
                    {
                        builder.objectProperty("exprs");
                        builder.property("value", transformExpression(astExpression, application, view, kid, "value", path, ExpressionType.VALUE,
                            scriptBuffer
                        ));
                        builder.close();
                    }
                    builder.close();
                }
                else
                {
                    builder.objectElement();
                    dumpComponentRecursive(builder, application, view, kid, kidPath, jsonFormat, scriptBuffer);
                }
            }
            builder.close();
        }
        builder.close();
    }


    private static boolean hasCursorProp(ComponentDescriptor descriptor)
    {
        for (PropDeclaration propDeclaration : descriptor.getPropTypes().values())
        {
            if (propDeclaration.getType() == PropType.CURSOR_EXPRESSION)
            {
                return true;
            }
        }
        return false;
    }


    private void addDefaults(
        JSONBuilder builder, View view, ComponentDescriptor descriptor, Set<String> defaultAttrs, boolean isExpression,
        ApplicationModel applicationModel, ComponentModel componentModel, ComponentRenderPath path,
        ScriptBuffer scriptBuffer
    )

    {
        Map<String, PropDeclaration> propTypes = descriptor.getPropTypes();
        for (String name : defaultAttrs)
        {
            PropDeclaration propDecl = propTypes.get(name);
            ExpressionValue defaultValue = propDecl.getDefaultValue();
            if ((defaultValue.getAstExpression() != null) == isExpression)
            {
                if (isExpression)
                {
                    builder.property(name, transformExpression(defaultValue.getAstExpression(), applicationModel, view, componentModel, name, path, getExpressionType(propDecl),
                        scriptBuffer));
                }
                else
                {
                    if (defaultValue.getType() == ExpressionValueType.EXPRESSION_ERROR)
                    {
                        builder.property(name, "[ERROR:" + defaultValue.getExpressionError() + "]");
                    }
                    else
                    {
                        builder.property(name, defaultValue.getValue());
                    }
                }
            }
        }
    }

    private Object transformExpression(
        ASTExpression expression,
        ApplicationModel applicationModel,
        View view,
        ComponentModel componentModel,
        String attrName,
        ComponentRenderPath path,
        ExpressionType type,
        ScriptBuffer scriptBuffer
    )
    {
        final Definitions definitions = applicationModel.lookup(view).getLocalDefinitions();

        final boolean isActionExpression = actionService != null && type == ExpressionType.ACTION;
        final boolean isCursorOrContextExpression = (
            type == ExpressionType.CURSOR ||
            type == ExpressionType.CONTEXT
        );

        final ComponentDescriptor componentDescriptor= componentModel.getComponentRegistration() != null ? componentModel.getComponentRegistration().getDescriptor() : null;



        ComponentRenderPath contextParent;
        PropDeclaration propDecl;
        if (componentDescriptor == null)
        {
            contextParent = findContext(path, null);
            propDecl = null;
            //throw new InvalidExpressionException("Invalid identifier 'context': <" + componentModel.getName() + " " + attrName + " > is not a cursor expression property");
        }
        else
        {
            propDecl = componentDescriptor.getPropTypes().get(attrName);
            contextParent = findContext(path, propDecl != null ? propDecl.getContextType() : null);
        }


        final List<ExpressionTransformer> transformers = DefaultExpressionTransformerFactory.createTransformers(
            type,
            applicationModel,
            definitions,
            new ComponentExpressionTransformer(componentModel, path, attrName, contextParent, propDecl),
            isActionExpression ?
                new ActionExpressionTransformer(definitions, false) :
                null
        );

        String output = renderer.transform(applicationModel,
            type, new ExpressionModelContext(view, componentModel, attrName), expression, transformers,
            scriptBuffer
        );
        if (isActionExpression)
        {
            return "function(){ return (" +  output + "); }";
        }
        else if (isCursorOrContextExpression)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("code", "return (" + output + ")");
            if (contextParent != null)
            {
                map.put("contextName", contextParent.getContextName());
                map.put("parent", contextParent.modelChainList());
            }
            else
            {
                map.put("contextName", null);
            }
            return map;
        }
        else
        {
            return output;
        }
    }


    private ComponentRenderPath findContext(
        ComponentRenderPath path, String contextType
    )
    {
        final ComponentRenderPath parent = path.getParent();
        if (parent == null)
        {
            return null;
        }
        return parent.findContextByType(contextType);
    }
}


