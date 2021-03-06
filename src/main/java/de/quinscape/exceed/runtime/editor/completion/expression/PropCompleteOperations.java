package de.quinscape.exceed.runtime.editor.completion.expression;


import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.MappingNode;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.ComponentModelBuilder;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.editor.completion.AceCompletion;
import de.quinscape.exceed.runtime.editor.completion.CompletionType;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Identifier;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import de.quinscape.exceed.runtime.expression.query.DataField;
import de.quinscape.exceed.runtime.expression.query.QueryContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;

@ExpressionOperations(environment = PropCompleteEnvironment.class)
public class PropCompleteOperations
{
    @Operation
    public List<AceCompletion> domainType(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        final RuntimeContext runtimeContext = ctx.getEnv().getRuntimeContext();

        List<AceCompletion> suggestions = new ArrayList<>();
        for (String domainType : runtimeContext.getDomainService().getDomainTypes().keySet())
        {
            suggestions.add(new AceCompletion(CompletionType.PROP, domainType, "DomainType", null));

        }
        return suggestions;
    }

    @Operation
    public List<AceCompletion> oneOf(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        List<AceCompletion> suggestions = new ArrayList<>();

        for (int i = 0; i < ctx.getASTFunction().jjtGetNumChildren(); i++)
        {
            Node kid = ctx.getASTFunction().jjtGetChild(i);

            Object value = kid.jjtAccept(ctx.getEnv(), null);
            suggestions.add(new AceCompletion(CompletionType.PROP, value.toString(), "Value", null));
        }
        return suggestions;
    }

    @Operation
    public List<AceCompletion> location(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        RuntimeApplication application = ctx.getEnv().getRuntimeContext().getRuntimeApplication();

        List<AceCompletion> suggestions = new ArrayList<>();

        MappingNode rootNode = application.getApplicationModel().getRoutingTable().getRootNode();
        collectLocations(ctx, suggestions, rootNode, new LocationPath(rootNode));
        return suggestions;
    }

    @Operation
    public List<AceCompletion> layout(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        final RuntimeContext runtimeContext = ctx.getEnv().getRuntimeContext();
        final ApplicationModel applicationModel = runtimeContext.getRuntimeApplication().getApplicationModel();
        final Set<String> layoutNames = applicationModel.getLayouts().keySet();

        List<AceCompletion> suggestions = new ArrayList<>();

        for (String name : layoutNames)
        {
            suggestions.add(new AceCompletion(CompletionType.PROP, name, "Layout", null));
        }
        return suggestions;
    }

    private void collectLocations(ExpressionContext<PropCompleteEnvironment> ctx, List<AceCompletion> suggestions, MappingNode node, LocationPath locationPath)
    {
        ComponentModel componentModel = ctx.getEnv().getComponentModel();
        Mapping mapping = node.getMapping();
        if (mapping != null)
        {


            String path = locationPath.path();

            final boolean isProcess = mapping.getProcessName() != null;
            if (isProcess)
            {
                path = path.substring(0, path.indexOf("/{stateId}"));
            }

            String currentText = componentModel.getAttribute("text").getValue();
            ComponentModelBuilder builder =
                component("Link")
                    .withAttribute("location", path)
                    .withAttribute("text", "${0:" + (currentText != null ? currentText : "text") + "}");


            Map<String, String> current = getCurrentParams(ctx);

            List<String> variables = locationPath.variables();
            for (int i = 0; i < variables.size(); i++)
            {
                String varName = variables.get(i);
                String value = current.get(varName);

                if (!isProcess || !varName.equals("stateId"))
                {
                    builder.withKids(
                        component("Link.Param")
                            .withAttribute("name", varName)
                            .withAttribute("value", "${" + (i + 1) + ":" + (value != null ? value : "value") + "}")
                    );
                }
            }

            suggestions.add(new AceCompletion(CompletionType.PROP, path, mapping
                .getName(), null, builder.getComponent(), null));
        }

        List<MappingNode> kids = node.getChildren();
        if (kids != null)
        {
            for (MappingNode kid : kids)
            {
                collectLocations(ctx, suggestions, kid, locationPath.next(kid));
            }
        }
    }


    private Map<String, String> getCurrentParams(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        ComponentModel componentModel = ctx.getEnv().getComponentModel();

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
                String varName = attrs.getAttribute("name").getValue();
                String value = attrs.getAttribute("value").getValue();
                map.put(varName, value);
            }
        }
        return map;
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

    @Operation
    public ComponentModel model(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        return ctx.getEnv().getComponentModel();
    }

    @Operation
    public ComponentModel parent(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        return  ctx.getEnv().getComponentModel().getParent();
    }

    @Operation(context = ComponentModel.class)
    public Object prop(ExpressionContext<PropCompleteEnvironment> ctx, ComponentModel componentModel, String name)
    {
        PropCompleteEnvironment env = ctx.getEnv();
        
        if (componentModel == null)
        {
            componentModel = env.getComponentModel();
        }
        ExpressionValue attribute = componentModel.getAttribute(name);
        if (attribute == null)
        {
            return null;
        }

        ASTExpression astExpression = attribute.getAstExpression();
        if (astExpression != null)
        {
            ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();
            PropDeclaration propDeclaration = componentRegistration.getDescriptor().getPropTypes().get(name);

            if (propDeclaration != null)
            {
                if (propDeclaration.getType() == PropType.QUERY_EXPRESSION)
                {
                    return env.getQueryTransformer().transform(
                        env.getRuntimeContext(),
                        new QueryContext(
                            env.getViewModel(),
                            componentModel,
                            Collections.emptyMap(),
                            null,
                            null
                        ),
                        astExpression
                    );
                }
            }

            // evaluate prop expression in our context
            return astExpression.jjtAccept(env, null);
        }
        else
        {
            return attribute.getValue();
        }
    }


    @Operation
    public List<AceCompletion> fieldOf(ExpressionContext<PropCompleteEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryDomainType queryDomainType = queryDefinition.getQueryDomainType();
        Set<String> alreadyUsedNames = findAlreadyUsed(ctx);

        List<AceCompletion> suggestions = new ArrayList<>();
        for (DataField dataField : queryDomainType.getJoinedFields())
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


    private Set<String> findAlreadyUsed(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        // so we've got a fieldOf() expression and we want to only complete
        // those fields who haven't already been used in a most general way without
        // having to hard-code lots of component name

        // we just assume that we're in a competition for field representation with
        // all siblings and their descendants on the same attribute we're querying

        ComponentModel componentModel = ctx.getEnv().getComponentModel();
        ComponentModel parent = componentModel.getParent();

        Set<String> used = new HashSet<>();

        parent.walk(component -> {

            // any component that has the name component name and is not us.
            if (component.getName().equals(componentModel.getName()) && component != componentModel)
            {
                // we get the same attribute we're completing for currently
                ExpressionValue attribute = component.getAttribute(ctx.getEnv().getPropName());
                if (attribute != null)
                {
                    ASTExpression astExpression = attribute.getAstExpression();
                    String name;
                    if (astExpression != null)
                    {
                        name = (String) astExpression.jjtAccept(ctx.getEnv(), null);
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

    @Identifier
    public ComponentModel parent(PropCompleteEnvironment env)
    {
        return env.getComponentModel().getParent();
    }

    @Identifier
    public ComponentModel model(PropCompleteEnvironment env)
    {
        return env.getComponentModel();
    }

    @Operation
    public List<AceCompletion> transitionNames(ExpressionContext<PropCompleteEnvironment> ctx)
    {
        PropCompleteEnvironment env = ctx.getEnv();
        View viewModel = env.getViewModel();
        String processName = viewModel.getProcessName();
        Process process = env.getRuntimeContext().getRuntimeApplication().getApplicationModel().getProcess(processName);

        ProcessState processState = process.getStates().get(viewModel.getName());
        if (processState instanceof ViewState)
        {
                List<AceCompletion> list = new ArrayList<>();
            for (String transitionName : ((ViewState) processState).getTransitions().keySet())
            {
                list.add(new AceCompletion(CompletionType.PROP, transitionName, "Transition", null));
            }
            return list;
        }
        return Collections.emptyList();
    }
}
