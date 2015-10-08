package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
        ViewData viewData = new ViewData(view.getName());
        DataProviderContext context = new DataProviderContext(this, runtimeContext, view.getName(), viewData);
        prepareRecursive(context, view.getRoot());
        return viewData;
    }

    void prepareRecursive(DataProviderContext context, ComponentModel element)
    {
        DataProvider dataProviderInstance = element.getDataProviderInstance();
        if (dataProviderInstance != null)
        {

            log.debug("Calling {} for {}", dataProviderInstance, element);

            context.enableContinueOnChildren();

            if (element.isComponent())
            {
                Map<String, Object> componentDataMap = dataProviderInstance.provide(context, element);
                context.getViewData().getComponentData().put(element.getComponentId(), componentDataMap != null ? componentDataMap : false);
            }

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
