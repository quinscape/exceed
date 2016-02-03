package de.quinscape.exceed.model.action;

import org.springframework.util.StringUtils;
import org.svenson.JSONProperty;

import java.beans.Introspector;

/**
 * Implemented by classes that parametrize {@link de.quinscape.exceed.runtime.action.Action} invocations.
 *
 * Action Models don't follow the normal model JSONification rules but resolve the concrete type by the symbolic name
 * give in the action property resolved via action registry.
 */
public abstract class ActionModel
{
    private final static String ACTION_SUFFIX = "ActionModel";

    private final String action;

    protected ActionModel()
    {
        this(null);
    }

    protected ActionModel(String action)
    {
        if (StringUtils.hasText(action))
        {
            this.action = action;
        }
        else
        {
            String name = this.getClass().getSimpleName();
            if (name.endsWith(ACTION_SUFFIX))
            {
                name = name.substring(0, name.length() - ACTION_SUFFIX.length());
            }

            this.action = Introspector.decapitalize(name);
        }
    }


    @JSONProperty(readOnly = true)
    public String getAction()
    {
        return action;
    }
}
