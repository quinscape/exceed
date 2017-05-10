package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.expression.query.QueryDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PreparedQueries
{
    private Map<String, QueryDefinition> queryDefinitions;

    private Map<String, QueryError> queryErrors;


    public void addDefinition(String name, QueryDefinition queryDefinition)
    {
        if (queryDefinitions == null)
        {
            queryDefinitions = new HashMap<>();
        }

        queryDefinitions.put(name,queryDefinition);
    }

    public void addError(String name, QueryError queryError)
    {
        if (queryErrors == null)
        {
            queryErrors = new HashMap<>();
        }

        queryErrors.put(name, queryError);
    }


    public Map<String, QueryDefinition> getQueryDefinitions()
    {
        if (queryDefinitions == null)
        {
            return Collections.emptyMap();
        }

        return queryDefinitions;
    }


    public Map<String, QueryError> getQueryErrors()
    {
        if (queryErrors == null)
        {
            return Collections.emptyMap();
        }
        return queryErrors;
    }


    public boolean hasErrors()
    {
        return queryErrors != null && queryErrors.size() > 0;
    }
}
