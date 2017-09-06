package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.scope.ScopedContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the necessary context for a view data provisioning invocation for
 * a view at runtime.
 */
public final class DataProviderContext
    implements Cloneable
{
    private final ViewDataService viewDataService;

    private final RuntimeContext runtimeContext;

    private final String viewName;

    private final ProcessExecutionState state;

    private final ViewData viewData;

    private boolean continueOnChildren = true;

    private final ComponentModel overridden;

    private final Map<String, Object> varsOverride;

    public DataProviderContext(ViewDataService viewDataService, RuntimeContext runtimeContext, String viewName, ProcessExecutionState state, ViewData viewData, ComponentModel overridden, Map<String, Object> varsOverride)
    {
        this.viewDataService = viewDataService;
        this.viewName = viewName;
        this.runtimeContext = runtimeContext;
        this.state = state;
        this.viewData = viewData;
        this.overridden = overridden;
        this.varsOverride = varsOverride;
    }

    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public String getViewName()
    {
        return viewName;
    }

    /**
     * Selectively invokes data providers for the given element node and its children and grand children.
     *
     * <p>
     *     Calling this method once will automatically cause the view data service to skip all children this method
     *     was not called for.
     * </p>
     *
     * @param element    element node
     */
    public void provideFor(ComponentModel element)
    {
        // we continue with a clone of ourselves to make sure that "continue on children" detection can happen
        // independently from this context
        viewDataService.prepareRecursive(this.clone(), element);

        // for this context, we now don't continue on children
        continueOnChildren = false;
    }

    protected DataProviderContext clone()
    {
        try
        {
            DataProviderContext clone = (DataProviderContext) super.clone();

            /**
             * we don't need to clone any of our members.
             * They're all either infrastructure or immutable.
             * Cloning {@link #componentData} would defeat the purpose of data collection.
             */

            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    /**
     * Enables continuing on the child nodes for the current node
     */
    void enableContinueOnChildren()
    {
        continueOnChildren = true;
    }

    /**
     * Returns whether the data provisioning will continue on the current node's children after providing for the
     * current node.
     *
     * @return  continue on children?
     */
    boolean isContinueOnChildren()
    {
        return continueOnChildren;
    }

    ViewData getViewData()
    {
        return viewData;
    }

    public String getProcessName()
    {
        return state.getExecution().getProcessName();
    }

    public ScopedContext getScopedContext()
    {
        return state.getScopedContext();
    }

    public Map<String, Object> getVars(ComponentModel componentModel)
    {
        if (componentModel == null)
        {
            throw new IllegalArgumentException("componentModel can't be null");
        }


        Map<String, Object> vars = new HashMap<>();

        final boolean isUpdatedComponent = componentModel.equals(overridden);

        ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();
        if (componentRegistration == null)
        {
            throw new IllegalStateException("No component registration for " + componentModel);
        }

        Map<String, ASTExpression> varExpressions = componentRegistration.getVarExpressions();

        if (varExpressions != null)
        {
            for (Map.Entry<String, ASTExpression> entry : varExpressions.entrySet())
            {
                String varName = entry.getKey();

                boolean useDefault = true;

                if (isUpdatedComponent)
                {
                    final Object v = varsOverride.get(varName);
                    if (v != null)
                    {
                        // use override value
                        vars.put(varName, v);
                        useDefault = false;
                    }
                }

                if (useDefault)
                {
                    ASTExpression astExpression = entry.getValue();
                    Object result = runtimeContext.getJsEnvironment().getValue(runtimeContext, astExpression);
                    vars.put(varName, result);
                }
            }
        }


        return vars;
    }


    public void registerTranslation(String tag)
    {
        viewData.registerTranslation(tag);
    }

    public void registerTranslations(RuntimeContext runtimeContext, DomainType type)
    {
        viewData.registerTranslations(runtimeContext, type);
    }


}
