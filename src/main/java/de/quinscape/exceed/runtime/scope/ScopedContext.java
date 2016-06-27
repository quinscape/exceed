package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedElementModel;
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

    default ScopedElementModel getModel(ScopedValueType type, String name)
    {
        switch (type)
        {

            case OBJECT:
                return getContextModel().getObjects().get(name);

            case LIST:
                return getContextModel().getLists().get(name);

            case PROPERTY:
                return getContextModel().getProperties().get(name);

            default:
                throw new IllegalStateException("Unhandled scoped value type: " + type);
        }
    }
}
