package de.quinscape.exceed.runtime.action;


import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;

/**
 * Implemented by the server-side of the unified action system.
 *
 * @param <M> action model type
 * @param <I> data input type.
 */
public interface Action<M extends ActionModel, I>
{
    Object execute(RuntimeContext runtimeContext, M model, I input) throws Exception;

    Class<M> getActionModelClass();

    Class<I> getInputClass();
}

