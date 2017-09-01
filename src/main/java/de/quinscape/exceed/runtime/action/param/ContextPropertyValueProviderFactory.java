package de.quinscape.exceed.runtime.action.param;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionUtil;
import de.quinscape.exceed.runtime.action.ExceedContext;
import de.quinscape.exceed.runtime.action.ParameterProvider;
import de.quinscape.exceed.runtime.action.ParameterProviderFactory;

import java.lang.annotation.Annotation;

/**
 * Parameter provider providing values out of the modeled contexts:
 *
 * <ul>
 *     <li> application context</li>
 *     <li> session context</li>
 *     <li> process context</li>
 *     <li> view context</li>
 * </ul>
 */
public class ContextPropertyValueProviderFactory
    implements ParameterProviderFactory
{
    public ParameterProvider createIfApplicable(Class<?> parameterClass, Annotation[] annotations)
    {
        final ExceedContext context = ActionUtil.find(annotations, ExceedContext.class);
        return context != null ? new Provider(context) : null;
    }

    private static class Provider
        implements ParameterProvider
    {
        private final ExceedContext context;

        public Provider(ExceedContext context)
        {

            this.context = context;
        }

        public Object provide(RuntimeContext runtimeContext)
        {
            return runtimeContext.getScopedContextChain().getProperty(context.value());
        }
    }
}
