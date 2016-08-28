package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;

import java.util.Map;

public interface ScopedContext
    extends ScopedResolver
{
    ContextModel getContextModel();

    ScopedContext copy(RuntimeContext runtimeContext);

    boolean isInitialized();

    void init(RuntimeContext runtimeContext, ExpressionService expressionService, ActionService actionService, Map<String,Object> inputValues);
}
