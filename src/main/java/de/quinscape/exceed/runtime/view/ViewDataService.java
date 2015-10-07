package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewDataService
{
    private static Logger log = LoggerFactory.getLogger(ViewDataService.class);

    /**
     * Prepares view data for the current view by recursively invoking the data providers registered
     * with all element nodes.
     *
     * The traversal of the element tree can be influenced by calling {@link DataProviderContext#provideFor(ComponentModel)}
     * from the called data providers.
     *
     * @param runtimeContext    runtime context
     * @param view              view model
     *
     * @return  view data
     */
    public ViewData prepareData(RuntimeContext runtimeContext, View view)
    {
        DataProviderContext context = new DataProviderContext(this, runtimeContext, view.getName());
        prepareRecursive(context, view.getRoot());
        return context.getViewData();
    }

    void prepareRecursive(DataProviderContext context, ComponentModel element)
    {
        DataProvider dataProviderInstance = element.getDataProviderInstance();
        if (dataProviderInstance != null)
        {
            context.enableContinueOnChildren();

            log.debug("Calling {} for {}", dataProviderInstance, element);

            dataProviderInstance.provide(context, element);

            if (context.isContinueOnChildren())
            {
                 for (ComponentModel kid : element.children())
                 {
                     prepareRecursive(context, kid);
                 }
            }
        }
    }
}
