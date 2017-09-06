package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.action.ActionService;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * Encapsulates context information for a component query transformation
 */
public class QueryContext
{
    private final ComponentModel componentModel;

    private final Map<String, Object> vars;

    private final Function<QueryDefinition, Object> executorFunction;

    private final ActionService actionService;

    private final View viewModel;


    public QueryContext(
        View viewModel,
        ComponentModel componentModel,
        Map<String, Object> vars,
        Function<QueryDefinition, Object> executorFunction,
        ActionService actionService
    )
    {
        this.viewModel = viewModel;
        this.componentModel = componentModel;
        this.vars = vars != null ? vars : Collections.emptyMap();
        this.executorFunction = executorFunction;
        this.actionService = actionService;
    }


    public ComponentModel getComponentModel()
    {
        return componentModel;
    }


    public View getViewModel()
    {
        return viewModel;
    }


    public Map<String, Object> getVars()
    {
        return vars;
    }


    /**
     * Internal function helper
     * 
     * @return
     */
    public Function<QueryDefinition, Object> getExecutorFunction()
    {
        return executorFunction;
    }


    public ActionService getActionService()
    {
        return actionService;
    }
}
