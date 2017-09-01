package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.util.JSONUtil;

import java.util.List;
import java.util.Map;

public interface ActionService
{
    ActionResult execute(RuntimeContext runtimeContext, String action, ActionParameters params);

    Map<String, ActionRegistration> getRegistrations();

    Definitions getActionFunctionDefinitions();

    static List<Object> parseArgs(String json)
    {
        final Map data = JSONUtil.DEFAULT_PARSER.parse(Map.class, json);
        if (data == null)
        {
            throw new InvalidActionParameterException("No action parameter data");
        }
        List<Object> args = (List<Object>) data.get("args");
        if (args == null)
        {
            throw new InvalidActionParameterException("Action parameter data must have an array property 'args'");
        }

        return args;
    }

}
