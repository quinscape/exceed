package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.action.Action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface ActionService
{
    Set<String> getActionNames();

    Action getAction(String name);

    default Map<String, Class<? extends ActionModel>> getActionModels()
    {
        Map<String, Class<? extends ActionModel>> actionModels = new HashMap<>();
        for (String action : getActionNames())
        {
            actionModels.put(action, getAction(action).getActionModelClass());
        }
        return Collections.unmodifiableMap(actionModels);
    }
}
