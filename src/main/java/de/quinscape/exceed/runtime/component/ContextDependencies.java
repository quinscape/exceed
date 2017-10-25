package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.JSONParameters;
import org.svenson.JSONable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates knowledge about effective var expressions containing references to context variables. If these
 * variables are updated, they automatically update the dependent components.
 *
 */
public class ContextDependencies
    implements JSONable
{
    private static final String COMPONENT_ID = "componentId";

    private static final String VAR_NAME = "varName";

    private final Map<String,Set<Map<String,Object>>> dependencies;


    public ContextDependencies(
        @JSONParameters
        Map<String, Set<Map<String,Object>>> dependencies
    )
    {
        this.dependencies = dependencies;
    }

    public void registerDependency(String contextName, String componentId, String varName)
    {
        final HashMap<String, Object> newEntry = new HashMap<>();
        newEntry.put(COMPONENT_ID, componentId);
        newEntry.put(VAR_NAME, varName);


        final Set<Map<String,Object>> existing = dependencies.get(contextName);
        if (existing == null)
        {
            Set<Map<String,Object>> newSet = new HashSet<>();
            newSet.add(newEntry);
            dependencies.put(contextName, newSet);
        }
        else
        {
            existing.add(newEntry);
        }
    }


    public Map<String,Set<Map<String,Object>>> getDependencies()
    {
        return dependencies;
    }


    @Override
    public String toJSON()
    {
        return JSONUtil.DEFAULT_GENERATOR.forValue(dependencies);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "dependencies = " + dependencies
            ;
    }
}
