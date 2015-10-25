package de.quinscape.exceed.component;

import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;
import java.util.Map;

/**
 * Descriptor for a single component within a component package.
 *
 * @see ComponentPackageDescriptor
 */
public class ComponentDescriptor
{
    private final Map<String,String> vars;
    private final Map<String,PropDeclaration> propTypes;
    private final List<String> childTypes;
    private final Map<String, Object> queries;
    private final List<ComponentTemplate> templates;
    private final String dataProviderName;

    private final Boolean contextProvider;

    private final String contextKey;

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

        @JSONParameter("dataProvider")
        String dataProviderName,

        @JSONParameter("childTypes")
        List<String> childTypes,

        @JSONParameter("queryExecutors")
        Map<String, String> queryExecutors,

        @JSONParameter("contextProvider")
        Boolean contextProvider,

        @JSONParameter("contextKey")
        String contextKey)

    {
        this.vars = vars;
        this.queries = Util.immutableMap(queries);
        this.propTypes = Util.immutableMap(propTypes);
        this.templates = Util.immutableList(templates);
        this.dataProviderName = dataProviderName;
        this.childTypes = Util.immutableList(childTypes);
        this.queryExecutors = Util.immutableMap(queryExecutors);
        this.contextProvider = contextProvider;
        this.contextKey = contextKey;
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


    public List<String> getChildTypes()
    {
        return childTypes;
    }


    @JSONProperty(ignoreIfNull = true)
    public boolean isContextProvider()
    {
        return contextProvider;
    }

    @JSONProperty(ignoreIfNull = true)
    public String getContextKey()
    {
        return contextKey;
    }


    public Map<String, String> getQueryExecutors()
    {
        return queryExecutors;
    }
}
