package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.action.Action;

import java.util.Set;

public interface ActionRegistry
{
    ActionModel resolve(String actionJSON);

    Set<String> getActionNames();

    Action getAction(String name);
}
