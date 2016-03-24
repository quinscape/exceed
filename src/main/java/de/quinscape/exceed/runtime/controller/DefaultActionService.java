package de.quinscape.exceed.runtime.controller;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.runtime.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class DefaultActionService
    implements ActionService
{
    private final static Logger log = LoggerFactory.getLogger(DefaultActionService.class);


    private final Map<String,? extends Action> actions;


    public DefaultActionService(Map<String, ? extends Action> actions)
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
