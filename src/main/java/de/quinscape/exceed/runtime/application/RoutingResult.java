package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.routing.Mapping;

import java.util.Map;

/**
 * Encapsulates the result of the routing table resolution process.
 */
public class RoutingResult
{
    private final Mapping mapping;

    private final Map<String, String> variables;

    private final String template;


    public RoutingResult(Mapping mapping, Map<String, String> variables, String template)
    {

        this.mapping = mapping;
        this.variables = variables;
        this.template = template;
    }


    public Mapping getMapping()
    {
        return mapping;
    }


    public Map<String, String> getVariables()
    {
        return variables;
    }


    public String getTemplate()
    {
        return template;
    }
}
