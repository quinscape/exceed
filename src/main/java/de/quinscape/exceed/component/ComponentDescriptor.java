package de.quinscape.exceed.component;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Descriptor for a single component within a component package.
 *
 * @see ComponentPackageDescriptor
 */
public class ComponentDescriptor
{
    private final Map<String,String> vars;
    private final Map<String,PropDeclaration> propTypes;
    private final String childRule;
    private final ASTExpression childRuleExpression;
    private final String parentRule;
    private final ASTExpression parentRuleExpression;
    private final Map<String, Object> queries;
    private final List<ComponentTemplate> templates;
    private final Set<String> classes;
    private final String dataProviderName;
    private final String providedContext;
    private final Map<String,ComponentPropWizard> componentPropWizards;

    private final boolean modelAware;

    private final Map<String,String> queryExecutors;

    public ComponentDescriptor(
        @JSONParameter("vars")
        Map<String, String> vars,

        @JSONParameter("queries")
        Map<String, Object> queries,

        @JSONParameter("propTypes")
        @JSONTypeHint(PropDeclaration.class)
        Map<String, PropDeclaration> propTypes,

        @JSONParameter("templates")
        @JSONTypeHint(ComponentTemplate.class)
        List<ComponentTemplate> templates,

        @JSONParameter("propWizards")
        @JSONTypeHint(ComponentPropWizard.class)
        Map<String,ComponentPropWizard> componentPropWizards,

        @JSONParameter("classes")
        List<String> classes,

        @JSONParameter("dataProvider")
        String dataProviderName,

        @JSONParameter("childRule")
        String childRule,

        @JSONParameter("queryExecutors")
        Map<String, String> queryExecutors,

        @JSONParameter("providesContext")
        String providedContext,

        @JSONParameter("modelAware")
        Boolean modelAware,

        @JSONParameter("parentRule")
        String parentRule) throws ParseException

    {
        this.vars = vars;
        this.parentRule = parentRule;
        this.modelAware = modelAware != null && modelAware;
        this.queries = Util.immutableMap(queries);
        this.propTypes = Util.immutableMap(propTypes);
        this.templates = Util.immutableList(templates);
        this.classes = Util.immutableSet(classes);
        this.dataProviderName = dataProviderName;
        this.childRule = childRule;
        this.queryExecutors = Util.immutableMap(queryExecutors);
        this.providedContext = providedContext;

        childRuleExpression = ExpressionParser.parse(childRule);
        parentRuleExpression = ExpressionParser.parse(parentRule);
        this.componentPropWizards = Util.immutableMap(componentPropWizards);
    }

    public Map<String, String> getVars()
    {
        return vars;
    }

    public Map<String, PropDeclaration> getPropTypes()
    {
        return propTypes;
    }

    public Map<String, Object> getQueries()
    {
        return queries;
    }


    public List<ComponentTemplate> getTemplates()
    {
        return templates;
    }


    public String getDataProviderName()
    {
        return dataProviderName;
    }


    public String getChildRule()
    {
        return childRule;
    }


    @JSONProperty(ignoreIfNull = true)

    public String getProvidedContext()
    {
        return providedContext;
    }

    public Map<String, String> getQueryExecutors()
    {
        return queryExecutors;
    }


    public boolean isModelAware()
    {
        return modelAware;
    }


    public Set<String> getClasses()
    {
        return classes;
    }

    public String getParentRule()
    {
        return parentRule;
    }

    @JSONProperty(ignore = true)
    public ASTExpression getChildRuleExpression()
    {
        return childRuleExpression;
    }


    @JSONProperty(ignore = true)
    public ASTExpression getParentRuleExpression()
    {
        return parentRuleExpression;
    }


    public Map<String, ComponentPropWizard> getComponentPropWizards()
    {
        return componentPropWizards;
    }
}
