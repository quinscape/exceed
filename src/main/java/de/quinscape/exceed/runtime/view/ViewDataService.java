package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
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
    public ViewData prepareView(RuntimeContext runtimeContext, View view)
    {
        ViewData viewData = new ViewData(runtimeContext, view.getName(), runtimeContext.getTranslator());
        DataProviderContext context = new DataProviderContext(this, runtimeContext, view.getName(), viewData, null, null);
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
     * @return  view data
     */
    public ViewData prepareComponent(RuntimeContext runtimeContext, View view, ComponentModel componentModel,
                                     Map<String, Object> vars)
    {
        Translator translator = runtimeContext.getTranslator();
        ViewData viewData = new ViewData(runtimeContext, view.getName(), runtimeContext.getTranslator());
        DataProviderContext context = new DataProviderContext(this, runtimeContext, view.getName(), viewData, componentModel, vars);
        prepareRecursive(context, componentModel);
        return viewData;
    }

    void prepareRecursive(DataProviderContext context, ComponentModel element)
    {
        ComponentRegistration componentRegistration = element.getComponentRegistration();
        if (element.isComponent() && componentRegistration != null)
        {
            DataProvider dataProviderInstance = (DataProvider) componentRegistration.getDataProvider();
            if (dataProviderInstance != null)
            {
                log.debug("Calling {} for {}", dataProviderInstance, element);

                context.enableContinueOnChildren();

                Map<String, Object> vars = context.getVars(element);
                Map<String, Object> componentDataMap = dataProviderInstance.provide(context, element, vars);


                ViewData viewData = context.getViewData();
                viewData.provide(element.getComponentId(), vars.size() > 0 ? vars : false, componentDataMap != null ? componentDataMap :false);

                if (context.isContinueOnChildren())
                {
                    for (ComponentModel kid : element.children())
                    {
                        prepareRecursive(context, kid);
                    }
                }
            }
        }
        else
        {
            for (ComponentModel kid : element.children())
            {
                prepareRecursive(context, kid);
            }
        }
    }
}
