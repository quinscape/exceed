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
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;
import org.svenson.util.JSONBuilder;

import java.util.List;
import java.util.Map;

public class ModelJSONServiceImpl
    implements ModelJSONService
{

    public static final String CONTEXT_DEFAULT_NAME = "context";

    public static final String MODEL_ATTR_NAME = "model";

    private static Logger log = LoggerFactory.getLogger(ModelJSONServiceImpl.class);

    private final JSONParser parser;

    private final JSON generator = JSON.defaultJSON();


    public ModelJSONServiceImpl(ModelFactory modelFactory)
    {
        parser = new JSONParser();
        parser.setTypeMapper(new ModelMapper());
        if (modelFactory != null)
        {
            parser.addObjectFactory(modelFactory);
        }
    }


    @Override
    public String toJSON(Object model)
    {
        if (model instanceof View)
        {
            return toJSON((View) model, JSONFormat.CLIENT);
        }
        return generator.forValue(model);
    }

    @Override
    public String toJSON(View model, JSONFormat jsonFormat)
    {
        if (jsonFormat == null)
        {
            throw new IllegalArgumentException("jsonFormat can't be null");
        }

        JSONBuilder builder = JSONBuilder.buildObject();
        {
            builder.property("_type", "view.View")
            .property("name", model.getName());

            String version = model.getVersion();
            if (version != null)
            {
                builder.property("version", version);
            }

            JSONBuilder.Level lvl = builder.getCurrentLevel();

            builder.objectProperty("root");
            {
                dumpComponentRecursive(builder, model.getRoot(), new ComponentPath(), jsonFormat);
                builder.closeUntil(lvl);
            }
            builder.property("comments", model.getComments());
        }
        return builder.output();
    }


    private void dumpComponentRecursive(JSONBuilder builder, ComponentModel componentModel, ComponentPath path, JSONFormat jsonFormat)
    {
        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        if (jsonFormat == JSONFormat.CLIENT && componentRegistration != null)
        {
            if (componentRegistration.getDescriptor().isContextProvider())
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

                        PropDeclaration propType;
                        if (componentRegistration == null ||
                            (propType = componentRegistration.getDescriptor().getPropTypes().get(attrName)) == null ||
                            propType.getContext() == null)
                        {
                            ClientExpressionRenderer visitor = new ClientExpressionRenderer(componentModel, path);
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

                provideContextExpressions(builder, componentModel, path, descriptor);

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
                        ClientExpressionRenderer renderer = new ClientExpressionRenderer(componentModel, path);
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
                    dumpComponentRecursive(builder, kid, kidPath, jsonFormat);
                }
                kidPath.increment();
            }
            builder.close();
        }
        builder.close();
    }


    private void provideContextExpressions(JSONBuilder builder, ComponentModel componentModel, ComponentPath path, ComponentDescriptor descriptor)
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
                        // otherwise we default to the first parent to provide a context
                        contextName = findContext(path.getParent());
                        renameIdentifier(contextAST, "context", contextName);
                    }

                    // .. and transform it for client consumption
                    ClientExpressionRenderer renderer = new ClientExpressionRenderer(componentModel, path);
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


    private String findContext(ComponentPath path)
    {
        while (path != null)
        {
            String contextName = path.getContextName();
            if (contextName != null)
            {
                return contextName;
            }
            path = path.getParent();
        }
        return null;
    }


    /**
     * Converts the given model JSON to a model instance.
     *
     * @param json JSON string. Must have a root "_type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public Model toModel(String json)
    {
        return parser.parse(Model.class, json);
    }


    /**
     * Converts the given model JSON to a model instance and validates it to be an expected type.
     *
     * @param cls  Expected (super) class.
     * @param json JSON string. Must have a root "_type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public <M extends Model> M toModel(Class<M> cls, String json) throws IllegalArgumentException
    {

        try
        {
            Model model = parser.parse(cls, json);
            if (!cls.isInstance(model))
            {
                throw new IllegalArgumentException("Expected " + cls.getSimpleName() + " but got " + json);
            }
            return (M) model;
        }
        catch (IllegalArgumentException e)
        {
            throw new ExceedRuntimeException("Error converting model JSON" + json, e);
        }
    }


    public static class ModelMapper
        extends AbstractPropertyValueBasedTypeMapper
    {
        public ModelMapper()
        {
            setDiscriminatorField("_type");
            setPathMatcher(new SubtypeMatcher(Model.class));
        }


        @Override
        protected Class getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            if (o == null)
            {
                return null;
            }

            try
            {
                return Class.forName(MODEL_PACKAGE + "." + o);
            }
            catch (ClassNotFoundException e)
            {
                throw new ExceedRuntimeException(e);
            }
        }
    }


    /**
     * Returns a type string for a model class.
     *
     * @param cls model class
     * @return type string used by domain object "_type" fields.
     */
    public static String getType(Class<? extends Model> cls)
    {
        String className = cls.getName();

        if (!className.startsWith(MODEL_PACKAGE) || className.charAt(MODEL_PACKAGE.length()) != '.')
        {
            throw new IllegalArgumentException(cls + " is not in package " + MODEL_PACKAGE);
        }

        return className.substring(MODEL_PACKAGE.length() + 1);
    }
}
