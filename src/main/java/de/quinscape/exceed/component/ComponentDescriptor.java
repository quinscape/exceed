package de.quinscape.exceed.component;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
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
    /**
     * Var definitions for this component
     */
    private final Map<String,String> vars;

    /**
     * Maps prop names to {@link PropDeclaration}s for this component.
     */
    private final Map<String,PropDeclaration> propTypes;

    /**
     * Completion rule to find all eligible children for this component. The general mechanism is to find all child candidates
     * with this rule and then use a potential parentRule on the child to validate the candidate status.
     *
     * @see #parentRule
     */
    private final String childRule;

    /**
     * AST for {@link #childRule}
     */
    private final ASTExpression childRuleExpression;

    /**
     * Validation rule to validate if a component is valid in a parent context.
     *
     * @see #childRule
     */
    private final String parentRule;

    /**
     * AST for {@link #parentRule}
     */
    private final ASTExpression parentRuleExpression;

    /**
     * Query definitions for this component.
     *
     * @see #dataProviderName
     */
    private final Map<String, Object> queries;

    /**
     * Template models for this component
     */
    private final List<ComponentTemplate> templates;

    /**
     * Component classification.
     */
    private final Set<String> classes;

    /**
     * Name of spring bean implementing the {@link DataProvider} interface to use as an alternate data provider to use for this component.
     * The default is to use {@link QueryDataProvider}.
     */
    private final String dataProviderName;
    /**
     * Defines the symbolic type name of the context provided by this component. Other components can receive that component type either
     * by inheriting it from the first parent to offer any context or by specifying a context type they depend on.
     */
    private final String providesContext;

    /**
     * Maps the prop name to the a component prop wizard definition for that prop.
     */
    private final Map<String,ComponentPropWizard> componentPropWizards;

    /**
     * If set to <code>true</code> the corresponding component will receive a <em>model</em> prop containing the original
     * model JSON data.
     */
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
        String providesContext,

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
        this.providesContext = providesContext;

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

    public String getProvidesContext()
    {
        return providesContext;
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
