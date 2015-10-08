package de.quinscape.exceed.component;

import org.svenson.JSONParameter;

import java.util.Map;

public class ComponentDescriptor
{
    private final Map<String,VarDeclaration> vars;
    private final Map<String,PropDeclaration> props;
    private final Map<String, Map<String, Object>> queries;


    public ComponentDescriptor(
        @JSONParameter("vars")
        Map<String, VarDeclaration> vars,
        @JSONParameter("queries")
        Map<String, Map<String,Object>> queries,
        @JSONParameter("props")
        Map<String, PropDeclaration> props
    )
    {
        this.vars = vars;
        this.props = props;
        this.queries = queries;
    }

    public Map<String, VarDeclaration> getVars()
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
}
