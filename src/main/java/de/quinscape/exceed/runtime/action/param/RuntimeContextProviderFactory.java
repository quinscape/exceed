package de.quinscape.exceed.runtime.action.param;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ParameterProvider;
import de.quinscape.exceed.runtime.action.ParameterProviderFactory;

import java.lang.annotation.Annotation;

public class RuntimeContextProviderFactory
    implements ParameterProviderFactory, ParameterProvider
{
    @Override
    public ParameterProvider createIfApplicable(Class<?> parameterClass, Annotation[] annotations)
    {

        return parameterClass.equals(RuntimeContext.class) ? this : null;
    }

    @Override
    public RuntimeContext provide(RuntimeContext runtimeContext)
    {
        return runtimeContext;
    }
}
