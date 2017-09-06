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
public class ApplicationDomainProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "domain";
    }


    @Override
    public ClientStateScope getScope()
    {
        // domain is constant over the runtime of an application.
        // In dev mode, we recreate a new application model for changes
        return ClientStateScope.APPLICATION;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        return new JSONData(
            JSONBuilder.buildObject()
                .property("domainTypes", applicationModel.getDomainTypes())
                .property("enumTypes", applicationModel.getEnums())
                .property("stateMachines", applicationModel.getStateMachines())
                .property("decimalConfig", applicationModel.getConfigModel().getDecimalConfig())
                .property("maxDecimalPlaces", applicationModel.getMetaData().getMaxDecimalPlaces())
                .output()
        );
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
