package de.quinscape.exceed.runtime.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultActionExecution
    implements ActionExecution
{
    private boolean resolved = true;

    private Map<String,Object> contextUpdate;

    public void updateContext(String name, Object value)
    {
        if (contextUpdate == null)
        {
            contextUpdate = new HashMap<>();
        }

        contextUpdate.put(name, value);
    }


    public Map<String, Object> getContextUpdate()
    {
        if (contextUpdate == null)
        {
            return Collections.emptyMap();
        }

        return contextUpdate;
    }


    @Override
    public void reject()
    {
        resolved = false;
    }

    @Override
    public boolean isResolved()
    {
        return resolved;
    }
}
