package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.ComponentTemplate;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.ComponentModelBuilder;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.editor.completion.expression.ChildRuleEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.editor.completion.expression.ParentComponent;
import de.quinscape.exceed.runtime.editor.completion.expression.ParentRuleEnvironment;
import de.quinscape.exceed.runtime.editor.completion.expression.PropCompleteEnvironment;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;

public class CompletionService
{
    /**
     * Meta description used for automatically generatted default component templates.
     */
    private static final String DEFAULT_TEMPLATE_META = "component";

    private static Logger log = LoggerFactory.getLogger(CompletionService.class);

    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private QueryTransformer queryTransformer;

    @Autowired
    private ExpressionService expressionService;


    public List<AceCompletion> autocomplete(RuntimeApplication runtimeApplication, List<ParentComponent> path, int index)
    {
        Set<String> parentClasses = new HashSet<>();

        ComponentDescriptor descriptor = null;

        if (path != null)
        {
            for (ParentComponent parentComponent : path)
            {
                descriptor = componentRegistry.getComponentRegistration(parentComponent
                    .getComponentName()).getDescriptor();
                parentClasses.addAll(descriptor.getClasses());
            }
        }

        if (descriptor == null)
        {
            throw new IllegalStateException("Invalid empty parent parent path");
        }

        List<AceCompletion> suggestions = new ArrayList<>();

        ASTExpression childRuleExpression = descriptor.getChildRuleExpression();
        if (childRuleExpression != null)
        {
            log.debug("Child-rule: {}", childRuleExpression);

            for (String componentName : componentRegistry.getComponentNames())
            {
                ComponentRegistration componentRegistration = componentRegistry.getComponentRegistration(componentName);
                ComponentDescriptor currentDescriptor = componentRegistration.getDescriptor();

                if (matchesRule(childRuleExpression, componentRegistration.getComponentName(), currentDescriptor, parentClasses))
                {
                    List<ComponentTemplate> templates = currentDescriptor.getTemplates();
                    if (templates.size() == 0)
                    {
                        suggestions.add(createDefaultSuggestion(componentName, currentDescriptor));
                    }
                    else
                    {
                        for (int i = 0; i < templates.size(); i++)
                        {
                            ComponentTemplate template = templates.get(i);

                            int wizardIndex = template.getWizard() != null ? i : -1;

                            suggestions.add(new AceCompletion(
                                CompletionType.COMPONENT,
                                componentName,
                                template.getTitle(),
                                template.getDescription(),
                                template.getComponentModel(),
                                new TemplateWizard(i)
                            ));
                        }
                    }
                }
            }
        }
        return suggestions;
    }

    private AceCompletion createDefaultSuggestion(String componentName, ComponentDescriptor descriptor)
    {

        ComponentModelBuilder builder = component(componentName);

        int varCount = 1;
        for (Map.Entry<String, PropDeclaration> entry : descriptor.getPropTypes().entrySet())
        {
            String propName = entry.getKey();
            PropDeclaration propDecl = entry.getValue();

            if (propDecl.isRequired())
            {
                builder.withAttribute(propName, "${" + (varCount++) + ":value}");
            }
        }
        return new AceCompletion(CompletionType.COMPONENT, componentName, DEFAULT_TEMPLATE_META, null,
            builder.getComponent(), null);
    }


    private boolean matchesRule(ASTExpression childRuleExpression, String componentName, ComponentDescriptor componentDescriptor, Set<String> parentClasses)
    {
        if (!(Boolean) expressionService.evaluate(childRuleExpression,  new ChildRuleEnvironment(componentName, componentDescriptor)))
        {
            return false;
        }

        ASTExpression parentRuleExpression = componentDescriptor.getParentRuleExpression();
        return parentRuleExpression == null || (Boolean) expressionService.evaluate(parentRuleExpression, new ParentRuleEnvironment(parentClasses));
    }


    public List<AceCompletion> autocompleteProp(RuntimeApplication runtimeApplication, String propName, View viewModel, List<Long> path)
    {
        ComponentModel componentModel = walkIndexPath(viewModel, path);

        ComponentUtil.updateComponentRegsAndParents(componentRegistry, viewModel, null);

        ComponentDescriptor componentDescriptor = componentModel.getComponentRegistration().getDescriptor();

        PropDeclaration propDeclaration = componentDescriptor.getPropTypes().get(propName);
        ASTExpression ruleExpression =  propDeclaration.getRuleExpression();
        if (ruleExpression == null)
        {
            throw new IllegalStateException("Cannot autocomplete prop declaration for '" + propName + "' as it has no rule expression");
        }

        PropCompleteEnvironment env = new PropCompleteEnvironment(
            runtimeApplication,
            queryTransformer,
            viewModel, componentModel, propName
        );

        return env.evaluate(expressionService);
    }

    public List<AceCompletion> autocompletePropName(RuntimeApplication runtimeApplication, View viewModel, List<Long> path)
    {
        ComponentModel componentModel = walkIndexPath(viewModel, path);

        ComponentUtil.updateComponentRegsAndParents(componentRegistry, viewModel, null);

        ComponentDescriptor componentDescriptor = componentModel.getComponentRegistration().getDescriptor();
        Map<String, PropDeclaration> propTypes = componentDescriptor.getPropTypes();

        Set<String> unusedPropNames = getUnusedPropNames(componentModel);

        List<AceCompletion> completions = new ArrayList<>();
        for (String propName : unusedPropNames)
        {
            PropDeclaration propDecl = propTypes.get(propName);

            String meta = propDecl.getType().name().toLowerCase() + (propDecl.isRequired() ? "*" : "");

            completions.add(new AceCompletion(CompletionType.PROP_NAME, propName, meta, propDecl.getDescription()));

        }

        return completions;
    }


    private Set<String> getUnusedPropNames(ComponentModel componentModel)
    {
        Map<String, PropDeclaration> propTypes = componentModel.getComponentRegistration().getDescriptor().getPropTypes();
        Set<String> unusedPropNames = new HashSet<>(propTypes.keySet());
        unusedPropNames.removeAll(componentModel.getAttrs().getNames());
        return unusedPropNames;
    }


    private ComponentModel walkIndexPath(View viewModel, List<Long> path)
    {
        ComponentModel componentModel = viewModel.getRoot();
        for (Long index : path)
        {
            componentModel = componentModel.getKids().get(index.intValue());
        }
        return componentModel;
    }


}
