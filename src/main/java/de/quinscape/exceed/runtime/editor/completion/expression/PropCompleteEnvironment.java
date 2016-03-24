package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.ComponentPropWizard;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.component.PropType;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.MappingNode;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.ComponentModelBuilder;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.RoutingResult;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.editor.completion.AceCompletion;
import de.quinscape.exceed.runtime.editor.completion.CompletionType;
import de.quinscape.exceed.runtime.editor.completion.PropWizard;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.Operation;
import de.quinscape.exceed.runtime.expression.query.DataField;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.service.ComponentRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.component;

/**
 * Expression environment for prop completion rules.
 *
 * @see de.quinscape.exceed.component.PropDeclaration#rule
 */
public class PropCompleteEnvironment
    extends ExpressionEnvironment
{
    private final String propName;

    private final View viewModel;

    private final ComponentModel componentModel;

    private final QueryTransformer queryTransformer;

    private final RuntimeApplication application;


    public PropCompleteEnvironment(RuntimeApplication application,
                                   QueryTransformer queryTransformer, View viewModel, ComponentModel componentModel, String propName)
    {
        this.application = application;
        this.queryTransformer = queryTransformer;
        this.propName = propName;
        this.viewModel = viewModel;
        this.componentModel = componentModel;

        allowEverything();
    }


    @Operation
    public List<AceCompletion> domainType(ASTFunction node)
    {
        List<AceCompletion> suggestions = new ArrayList<>();
        for (String domainType : application.getDomainService().getDomainTypeNames())
        {
            suggestions.add(new AceCompletion(CompletionType.PROP, domainType, "DomainType", null));

        }
        return suggestions;
    }

    @Operation
    public List<AceCompletion> location(ASTFunction node)
    {
        List<AceCompletion> suggestions = new ArrayList<>();

        MappingNode rootNode = application.getApplicationModel().getRoutingTable().getRootNode();
        collectLocations(suggestions, rootNode, new LocationPath(rootNode));
        return suggestions;
    }

    private void collectLocations(List<AceCompletion> suggestions, MappingNode node, LocationPath locationPath)
    {
        Mapping mapping = node.getMapping();
        if (mapping != null)
        {
            String path = locationPath.path();
            String currentText = componentModel.getAttribute("text").getValue();
            ComponentModelBuilder builder =
                component("Link")
                    .withAttribute("location", path)
                    .withAttribute("text", "${0:" + (currentText != null ? currentText : "text") + "}");


            Map<String, String> current = getCurrentParams();

            List<String> variables = locationPath.variables();
            for (int i = 0; i < variables.size(); i++)
            {
                String varName = variables.get(i);
                String value = current.get(varName);

                builder.withKids(
                    component("Link.Param")
                        .withAttribute("name", varName)
                        .withAttribute("value", "${" + (i + 1) + ":" + (value != null ? value : "value") + "}")
                );
            }

            suggestions.add(new AceCompletion(CompletionType.PROP, path, mapping
                .getName(), null, builder.getComponent(), null));
        }

            List<MappingNode> kids = node.getChildren();
            if (kids != null)
            {
                for (MappingNode kid : kids)
                {
                    collectLocations(suggestions, kid, locationPath.next(kid));
                }
            }
    }


    private Map<String, String> getCurrentParams()
    {
        if (componentModel == null || componentModel.getKids() == null)
        {
            return Collections.emptyMap();
        }

        Map<String,String> map = new HashMap<>();
        for (ComponentModel kid : componentModel.getKids())
        {
            if (kid.getName().equals("Link.Param"))
            {
                Attributes attrs = kid.getAttrs();
                String varName = (String) attrs.getAttribute("name").getValue();
                String value = (String) attrs.getAttribute("value").getValue();
                map.put(varName, value);
            }
        }
        return map;
    }

    @Operation
    public List<AceCompletion> locationParams(ASTFunction node)
    {
        Set<String> alreadyUsedNames = findAlreadyUsed();

        List<AceCompletion> suggestions = new ArrayList<>();

        Object value = node.jjtGetChild(0).jjtAccept(this, null);

        if (!(value instanceof String))
        {
            throw new IllegalArgumentException("Path is no string: " + value);
        }
        String path = (String) value;

        RoutingResult result = application.getApplicationModel().getRoutingTable().resolve(path);

        for (String name : result.getVariables().keySet())
        {
            if (!alreadyUsedNames.contains(name))
            {
                suggestions.add(new AceCompletion(CompletionType.PROP, name, "param", null));
            }
        }

        return suggestions;
    }



    @Operation
    public List<AceCompletion> integer(ASTFunction node)
    {
        return Collections.singletonList(new AceCompletion(CompletionType.PROP, "{ ${0:" + propName + "} }", "Integer", null));
    }

    @Operation
    public List<AceCompletion> fieldOf(ASTFunction node)
    {
        Set<String> alreadyUsedNames = findAlreadyUsed();

        QueryDomainType queryDomainType = getArg(node, 0, QueryDomainType.class);

        List<AceCompletion> suggestions = new ArrayList<>();
        for (DataField dataField : queryDomainType.getDomainTypeFields())
        {
            boolean isSimpleQuery = queryDomainType.getJoinedType() == null;
            String fieldName = isSimpleQuery ?  fieldName(dataField.getLocalName()) : dataField.getLocalName();
            if (!alreadyUsedNames.contains(fieldName))
            {
                suggestions.add(new AceCompletion(CompletionType.PROP, fieldName, isSimpleQuery ? dataField.getQualifiedName() : null, null));
            }
        }

        return suggestions;
    }


    private String fieldName(String localName)
    {
        int lastDot = localName.lastIndexOf('.');
        return lastDot < 0 ? localName : localName.substring(lastDot + 1);
    }


    private Set<String> findAlreadyUsed()
    {
        // so we've got a fieldOf() expression and we want to only complete
        // those fields who haven't already been used in a most general way without
        // having to hard-code lots of component name

        // we just assume that we're in a competition for field representation with
        // all siblings and their descendants on the same attribute we're querying

        ComponentModel parent = componentModel.getParent();

        Set<String> used = new HashSet<>();

        parent.walk(component -> {

            // any component that has the name component name and is not us.
            if (component.getName().equals(componentModel.getName()) && component != componentModel)
            {
                // we get the same attribute we're completing for currently
                AttributeValue attribute = component.getAttribute(propName);
                if (attribute != null)
                {
                    ASTExpression astExpression = attribute.getAstExpression();
                    String name;
                    if (astExpression != null)
                    {
                        name = (String) astExpression.jjtAccept(this, null);
                    }
                    else
                    {
                        name = attribute.getValue().toString();
                    }

                    // .. and add its name to our set
                    used.add(name);
                }
            }
        });
        return used;
    }


    @Operation
    public ComponentModel model(ASTFunction node)
    {
        return  componentModel;
    }

    @Operation
    public ComponentModel parent(ASTFunction node)
    {
        return  componentModel.getParent();
    }

    @Operation
    public Object prop(ASTFunction node)
    {
        return  prop(node, componentModel);
    }

    @Operation
    public Object prop(ASTFunction node, ComponentModel componentModel)
    {
        String name = getArg(node, 0, String.class);

        AttributeValue attribute = componentModel.getAttribute(name);
        if (attribute == null)
        {
            return null;
        }

        ASTExpression astExpression = attribute.getAstExpression();
        if (astExpression != null)
        {
            ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
            PropDeclaration propDeclaration = componentRegistration.getDescriptor().getPropTypes().get(name);

            if (propDeclaration != null)
            {
                if (propDeclaration.getType() == PropType.QUERY_EXPRESSION)
                {
                    return  queryTransformer.evaluate(
                        application.getDomainService(),
                        astExpression,
                        componentModel,
                        // TODO: vars?
                        Collections.emptyMap());
                }
            }

            // evaluate prop expression in our context
            return astExpression.jjtAccept(this, null);
        }
        else
        {
            return attribute.getValue();
        }
    }


    @Override
    protected Object resolveIdentifier(String name)
    {
        if (name.equals("parent"))
        {
            return componentModel.getParent();
        }
        else if (name.equals("model"))
        {
            return componentModel.getParent();
        }

        return super.resolveIdentifier(name);
    }


    public List<AceCompletion> evaluate()
    {
        ComponentDescriptor descriptor = componentModel.getComponentRegistration().getDescriptor();
        ASTExpression ruleExpression = descriptor.getPropTypes().get(propName).getRuleExpression();

        Object o = ruleExpression.jjtAccept(this, null);

        if (o instanceof List)
        {
            List<AceCompletion> list = (List<AceCompletion>) o;
            if (list.size() == 0 || list.get(0) != null)
            {
                ComponentPropWizard componentPropWizard = descriptor.getComponentPropWizards().get(propName);
                if (componentPropWizard != null)
                {

                    list.add(new AceCompletion(CompletionType.PROP, "...", componentPropWizard.getTitle(), componentPropWizard.getDescription(), null, new PropWizard(propName)));
                }

                return list;
            }
        }

        throw new IllegalStateException(ExpressionRenderer.render(ruleExpression) + " produced no list of prop suggestions");
    }


    private class LocationPath
    {
        private final MappingNode node;
        private final LocationPath parent;


        private LocationPath(MappingNode node)
        {
            this(node, null);
        }

        private LocationPath(MappingNode node, LocationPath parent)
        {
            this.node = node;
            this.parent = parent;
        }

        public LocationPath next(MappingNode kid)
        {
            return new LocationPath(kid, this);
        }

        public String path()
        {
            if (this.parent == null)
            {
                return "/";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("/").append(this.node.getName());

            LocationPath p = this;

            while ((p = p.parent)  != null)
            {
                if (p.parent != null)
                {
                    sb.insert(0, "/" + p.node.getName());
                }
            }

            return sb.toString();
        }

        public List<String> variables()
        {
            List<String> vars = new ArrayList<>();

            LocationPath p = this;
            while (p  != null)
            {
                String varName = p.node.getVarName();
                if (varName != null)
                {
                    vars.add(varName);
                }
                p = p.parent;
            }
            Collections.reverse(vars);
            return vars;
        }
    }
}
