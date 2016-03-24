package de.quinscape.exceed.runtime.controller;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultActionRegistry
    implements ActionRegistry
{
    private final static Logger log = LoggerFactory.getLogger(DefaultActionRegistry.class);


    private final Map<String,? extends Action> actions;


    public DefaultActionRegistry(Map<String, ? extends Action> actions)
    {
        this.actions = ImmutableMap.copyOf(actions);
        log.info("Created action registery with actions = {}", getActionNames());
    }

//    @Override
//    public ActionModel fromJSON(String actionJSON)
//    {
//        return jsonParser.parse(ActionModel.class, actionJSON);
//    }

    @Override
    public Set<String> getActionNames()
    {
        return actions.keySet();
    }

    @Override
    public Action getAction(String name)
    {
        Action action = actions.get(name);
        if (action == null)
        {
            throw new ActionNotFoundException("Invalid action name '" + name + "'");
        }

        return action;
    }
}
