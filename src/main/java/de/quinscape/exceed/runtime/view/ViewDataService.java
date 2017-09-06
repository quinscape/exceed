package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.DataProviderPreparationException;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ViewDataService
{
    private final static Logger log = LoggerFactory.getLogger(ViewDataService.class);

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
     * @param state
     * @return  view data
     */
    public ViewData prepareView(RuntimeContext runtimeContext, View view, ProcessExecutionState state)
    {
        ViewData viewData = new ViewData();
        DataProviderContext context = new DataProviderContext(this, runtimeContext, view.getName(), state, viewData, null, null);

        for (ComponentModel componentModel : view.getContent().values())
        {
            prepareRecursive(context, componentModel);
        }
        return viewData;
    }



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
     * @param vars
     * @param state
     * @return  view data
     */
    public ComponentData prepareComponent(RuntimeContext runtimeContext, View view, ComponentModel componentModel,
                                          Map<String, Object> vars, ProcessExecutionState state)
    {
        ViewData viewData = new ViewData();
        DataProviderContext context = new DataProviderContext(this, runtimeContext, view.getName(), state, viewData, componentModel, vars);
        prepareRecursive(context, componentModel);
        return viewData.getComponentData().get(componentModel.getComponentId());
    }

    void prepareRecursive(DataProviderContext context, ComponentModel element)
    {
        ComponentInstanceRegistration componentRegistration = element.getComponentRegistration();
        if (element.isComponent() && componentRegistration != null)
        {
            DataProvider dataProviderInstance = componentRegistration.getDataProvider();
            if (dataProviderInstance != null)
            {
                prepareComponent(context, element, dataProviderInstance);

                if (context.isContinueOnChildren())
                {
                    for (ComponentModel kid : element.children())
                    {
                        prepareRecursive(context, kid);
                    }
                }
                return;
            }
        }

        for (ComponentModel kid : element.children())
        {
            prepareRecursive(context, kid);
        }
    }


    private void prepareComponent(
        DataProviderContext context, ComponentModel element, DataProvider dataProviderInstance
    )
    {
        final String componentId = element.getComponentId();
        try
        {
            log.debug("Calling {} for {}", dataProviderInstance, element);

            context.enableContinueOnChildren();

            final Map<String, Object> vars = context.getVars(element);
            final Map<String, Object> componentDataMap = dataProviderInstance.provide(context, element, vars);

            final ViewData viewData = context.getViewData();

            final Object varsArg = vars.size() > 0 ? vars : false;

            if (componentDataMap != null)
            {
                viewData.provide(componentId, new ComponentData(varsArg, componentDataMap));
            }
            else
            {
                viewData.provide(componentId, new ComponentData(varsArg, false));
            }
        }
        catch(Exception e)
        {
            throw new DataProviderPreparationException(componentId,"Error providing for " + element + " with " + dataProviderInstance + ", context = " + context, e);
        }
    }
}
