package de.quinscape.exceed.model.component;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.annotation.DocumentedCollection;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Descriptor for a single component within a component package. It contains a formal description of component properties and
 * their types. It is used by the server side to compose components in the editor, to provide the declared queries and call the declared
 * data providers and by the client-side view-renderer module to render a method producing a reactjs component tree from the
 * JSON model description of the view.
 *
 * @see ComponentPackageDescriptor
 * @see PropDeclaration
 */
public class ComponentDescriptor
{
    private final Map<String,String> vars;

    private final Map<String,PropDeclaration> propTypes;

    private final String childRule;

    /**
     * AST for {@link #childRule}
     */
    private final ASTExpression childRuleExpression;

    private final String parentRule;

    /**
     * AST for {@link #parentRule}
     */
    private final ASTExpression parentRuleExpression;

    private final Map<String, Object> queries;

    private final List<ComponentTemplate> templates;

    private final Set<String> classes;

    private final String dataProvider;

    private final String providesContext;

    private final Map<String,ComponentPropWizard> componentPropWizards;

    private final String description;

    private final String queryTransformerName;

    private ComponentPackageDescriptor packageDescriptor;


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
            Map<String, ComponentPropWizard> componentPropWizards,

        @JSONParameter("classes")
            List<String> classes,

        @JSONParameter("dataProvider")
            String dataProvider,

        @JSONParameter("childRule")
            String childRule,

        @JSONParameter("providesContext")
            String providesContext,

        @JSONParameter("parentRule")
            String parentRule,

        @JSONParameter("description")
        String description,

        @JSONParameter("queryTransformer")
        String queryTransformerName
    ) throws ParseException

    {
        this.vars = vars;
        this.parentRule = parentRule;
        this.queries = Util.immutableMap(queries);

        this.propTypes = Util.immutableMap(propTypes);

        this.templates = Util.immutableList(templates);
        this.classes = Util.immutableSet(classes);
        this.dataProvider = dataProvider;
        this.childRule = childRule;
        this.providesContext = providesContext;

        childRuleExpression = ExpressionParser.parse(childRule);
        parentRuleExpression = ExpressionParser.parse(parentRule);
        this.componentPropWizards = Util.immutableMap(componentPropWizards);

        this.description = description;
        this.queryTransformerName = queryTransformerName;


        if (this.propTypes.containsKey(ComponentModel.VAR_ATTRIBUTE))
        {
            throw new InconsistentModelException("'var' is a reserved property name.");
        }
    }


    /**
     * Var definitions for this component
     */
    public Map<String, String> getVars()
    {
        return vars;
    }

    /**
     * Maps prop names to {@link PropDeclaration}s for this component.
     */
    @JSONTypeHint(PropDeclaration.class)
    @DocumentedCollection(keyDesc = "propName")
    public Map<String, PropDeclaration> getPropTypes()
    {
        return propTypes;
    }

    /**
     * Query definitions for this component. The result of the query will show up in the component data block under
     * the defined query name.
     */
    @DocumentedModelType("queryName")
    public Map<String, Object> getQueries()
    {
        return queries;
    }


    /**
     * Template models for this component
     */
    @JSONTypeHint(ComponentTemplate.class)
    public List<ComponentTemplate> getTemplates()
    {
        return templates;
    }

    /**
     * Name of spring bean implementing the {@link DataProvider} interface to use as an alternate data provider to use for this component.
     * The default is to use {@link QueryDataProvider}.
     */
    public String getDataProvider()
    {
        return dataProvider;
    }


    /**
     * Completion rule to find all eligible children for this component. The general mechanism is to find all child candidates
     * with this rule and then use a potential parentRule on the child to validate the candidate status.
     */
    @DocumentedModelType("Expression")
    public String getChildRule()
    {
        return childRule;
    }


    /**
     * Defines the symbolic type name of the context provided by this component. Other components can receive that component type either
     * by inheriting it from the first parent to offer any context or by specifying a context type they depend on.
     */
    @JSONProperty(ignoreIfNull = true)
    public String getProvidesContext()
    {
        return providesContext;
    }

    /**
     * Component classification for this component. The classes are used to determine eligibility for completion or
     * to activate view renderer features.
     * <p>
     *     The class "model-aware" will cause the model and viewModel to be injected into the component.
     * </p>
     */
    public Set<String> getClasses()
    {
        return classes;
    }

    /**
     * Validation rule to validate if a component is valid in a parent context.
     *
     * @see #childRule
     */
    @DocumentedModelType("Expression")
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


    /**
     * Maps the prop name to the a component prop wizard definition for that prop.
     */
    @JSONTypeHint(ComponentPropWizard.class)
    public Map<String, ComponentPropWizard> getComponentPropWizards()
    {
        return componentPropWizards;
    }

    public boolean hasClass(String cls)
    {
        return classes.contains(cls);
    }


    /**
     * Description for the component.
     *
     * @return
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Bean name of a {@link de.quinscape.exceed.runtime.expression.query.QueryTransformer} implementation
     * (Default is "componentQueryTransformer")
     *
     * @return
     */
    public String getQueryTransformer()
    {
        return queryTransformerName;
    }


    @JSONProperty(ignore = true)
    public void setPackageDescriptor(ComponentPackageDescriptor packageDescriptor)
    {
        this.packageDescriptor = packageDescriptor;
    }


    public ComponentPackageDescriptor getPackageDescriptor()
    {
        return packageDescriptor;
    }


    public void validate()
    {
        if (hasClass(ComponentClasses.FIELD) && !hasClass(ComponentClasses.MODEL_AWARE))
        {
            throw new InconsistentModelException("Component declares \"field\" class but not \"model-aware\": " + this);
        }
    }
}
