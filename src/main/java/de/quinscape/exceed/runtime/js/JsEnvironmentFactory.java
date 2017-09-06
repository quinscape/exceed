package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.action.ActionService;
import jdk.nashorn.api.scripting.NashornScriptEngine;

/**
 * Creates a js environment for an application model.
 */
public class JsEnvironmentFactory
{
    private final ActionService actionService;
    private final NashornScriptEngine nashorn;
    private final ExpressionCompiler compiler;


    public JsEnvironmentFactory(
        ActionService actionService,
        NashornScriptEngine nashorn,
        ExpressionCompiler compiler
    )
    {
        this.actionService = actionService;
        this.nashorn = nashorn;
        this.compiler = compiler;
    }


    /**
     * Returns a new js environment for the given application model.
     *
     * @param applicationModel      application model
     *                              
     * @return js environment
     */
    public JsEnvironment create(ApplicationModel applicationModel)
    {
        return new JsEnvironment(actionService, nashorn, applicationModel, compiler);
    }
}
