package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.view.ViewData;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@ExceedAppProvider
@ExceedEditorProvider
public class ApplicationConfigProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "config";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.APPLICATION;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();
        
        return new JSONData(JSONBuilder.buildObject()
            
            .property("appName", applicationModel.getName())
            .property("defaultCurrency", applicationModel.getConfigModel().getDefaultCurrency())
            .property("component", runtimeContext.getApplicationModel().getConfigModel().getComponentConfig())

            .output());
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
