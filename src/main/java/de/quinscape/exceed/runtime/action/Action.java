package de.quinscape.exceed.runtime.action;


import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;

/**
 * Implemented by the server-side of the unified action system.
 *
 * @param <M> action model type
 */
public interface Action<M extends ActionModel>
{
    void execute(RuntimeContext runtimeContext, M model) throws Exception;

    default Class<M> getActionModelClass()
    {
        return (Class<M>) ActionModel.class;
    }
}

