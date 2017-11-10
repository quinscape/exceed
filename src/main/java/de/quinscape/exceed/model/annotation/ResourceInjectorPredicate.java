package de.quinscape.exceed.model.annotation;

import de.quinscape.exceed.runtime.RuntimeContext;

public interface ResourceInjectorPredicate
{
    boolean shouldInject(RuntimeContext runtimeContext);
}
