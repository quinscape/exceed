package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;

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
    private final ViewData viewData;

    private boolean continueOnChildren = true;

    public DataProviderContext(ViewDataService viewDataService, RuntimeContext runtimeContext, String viewName, ViewData viewData)
    {
        this.viewDataService = viewDataService;
        this.viewName = viewName;
        this.runtimeContext = runtimeContext;
        this.viewData = viewData;
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
}
