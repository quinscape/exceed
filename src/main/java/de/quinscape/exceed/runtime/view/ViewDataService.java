package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ViewDataService
{
    private final static Logger log = LoggerFactory.getLogger(ViewDataService.class);

    @Autowired
    private ExpressionService expressionService;

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
        ViewData viewData = new ViewData(runtimeContext, view.getName());
        DataProviderContext context = new DataProviderContext(this, runtimeContext, expressionService, view.getName(), state, viewData, null, null);
        prepareRecursive(context, view.getRoot());
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
        ViewData viewData = new ViewData(runtimeContext, view.getName());
        DataProviderContext context = new DataProviderContext(this, runtimeContext, expressionService, view.getName(), state, viewData, componentModel, vars);
        prepareRecursive(context, componentModel);
        return (ComponentData) viewData.getData().get(componentModel.getComponentId());
    }

    void prepareRecursive(DataProviderContext context, ComponentModel element)
    {
        ComponentRegistration componentRegistration = element.getComponentRegistration();
        if (element.isComponent() && componentRegistration != null)
        {
            DataProvider dataProviderInstance = componentRegistration.getDataProvider();
            if (dataProviderInstance != null)
            {
                log.debug("Calling {} for {}", dataProviderInstance, element);

                context.enableContinueOnChildren();

                Map<String, Object> vars = context.getVars(element);
                Map<String, Object> componentDataMap = dataProviderInstance.provide(context, element, vars);

                ViewData viewData = context.getViewData();
                String componentId = element.getComponentId();

                viewData.provide(componentId, vars.size() > 0 ? vars : false, componentDataMap != null ? componentDataMap :false);

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
}
