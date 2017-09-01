package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.annotation.ExceedPropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

/**
 * Encapsulates the information for a single action parameter.
 */
final class ParameterInfo
{
    /**
     * Index of the positional parameter or <code>-1</code> if it is a parameter with parameter provider
     */
    public final int index;

    /**
     * Parameter provider instance for the parameter. Only set when {@link #index} is <code>-1</code>
      */
    public final ParameterProvider provider;

    /**
     * <code>true</code> if the parameter set to required via {@link ExceedPropertyType} annotation
     */
    public final boolean isRequired;


    public ParameterInfo(int index, boolean isRequired)
    {

        this.index = index;
        this.provider = null;
        this.isRequired = isRequired;
    }


    public ParameterInfo(ParameterProvider provider, boolean isRequired)
    {
        if (provider == null)
        {
            throw new IllegalArgumentException("provider can't be null");
        }

        this.index = -1;
        this.provider = provider;
        this.isRequired = isRequired;
    }

    public Object provide(
        RuntimeContext runtimeContext, List<?> args, ActionExecution actionExecution
    )
    {

        final Object result;
        if (provider != null)
        {
            if (provider == ActionExecutionPlaceholder.INSTANCE)
            {
                return actionExecution;
            }

            result = provider.provide(runtimeContext);
        }
        else
        {
            result = index < args.size() ? args.get(index) : null;
        }

        if (result == null && isRequired)
        {
            throw new InvalidActionParameterException("@NotNull parameter is null");
        }
        return result;
    }
}
