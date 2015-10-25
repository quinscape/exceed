package de.quinscape.exceed.component;

import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONParameter;

import java.util.Map;

/**
 * Descriptor for a single component within a component package.
 *
 * @see ComponentPackageDescriptor
 */
public class ComponentDescriptor
{
    private final Map<String,VarDeclaration> vars;
    private final Map<String,PropDeclaration> props;
    private final Map<String, Map<String, Object>> queries;

    private final Map<String,String> queryExecutors;

    public ComponentDescriptor(
        @JSONParameter("vars")
        Map<String, VarDeclaration> vars,
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
    {
        this.vars = vars;
        this.propTypes = Util.immutableMap(propTypes);
        this.queries = Util.immutableMap(queries);
        this.childTypes = Util.immutableList(childTypes);
        this.templates = Util.immutableList(templates);
        this.queryExecutors = Util.immutableMap(queryExecutors);
        this.dataProviderName = dataProviderName;
    }




    public Map<String, String> getVars()
    {
        return vars;
    }

    public Map<String, PropDeclaration> getProps()
    {
        return props;
    }

    public Map<String, Map<String, Object>> getQueries()
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


    public Map<String, String> getQueryExecutors()
    {
        return queryExecutors;
    }
}
