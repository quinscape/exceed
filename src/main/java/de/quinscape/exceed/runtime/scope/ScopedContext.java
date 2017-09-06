package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;

import java.util.Map;

public interface ScopedContext
    extends ScopedResolver
{
    ContextModel getContextModel();

    ScopedContext copy(RuntimeContext runtimeContext);

    boolean isInitialized();

    void init(RuntimeContext runtimeContext, JsEnvironment jsEnvironment, Map<String,Object> inputValues);
}
