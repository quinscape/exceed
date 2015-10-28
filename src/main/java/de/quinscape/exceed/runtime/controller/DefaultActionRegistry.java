package de.quinscape.exceed.runtime.controller;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import java.util.Map;
import java.util.Set;

public class DefaultActionRegistry
    implements ActionRegistry
{
    private static Logger log = LoggerFactory.getLogger(DefaultActionRegistry.class);


    private final Map<String,? extends Action> actions;

    private final JSONParser jsonParser;

    public DefaultActionRegistry(Map<String, ? extends Action> actions)
    {
        this.actions = ImmutableMap.copyOf(actions);

        jsonParser = new JSONParser();
        jsonParser.setTypeMapper(new ActionMapper());

        log.info("Created action registery with actions = {}", getActionNames());
    }

    @Override
    public ActionModel resolve(String actionJSON)
    {
        return jsonParser.parse(ActionModel.class, actionJSON);
    }

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


    private class ActionMapper
        extends AbstractPropertyValueBasedTypeMapper
    {
        public ActionMapper()
        {
            setDiscriminatorField("action");
            setPathMatcher(new SubtypeMatcher(ActionModel.class));
        }

        @Override
        protected Class<? extends ActionModel> getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            if (o instanceof String)
            {
                Class cls = actions.get(o).getActionModelClass();
                if (cls == null)
                {
                    throw new ActionNotFoundException("Invalid action reference in JSON: " + o);
                }
                return cls;
            }

            throw new ActionNotFoundException("Invalid action discriminator value: " + o);
        }
    }


}
