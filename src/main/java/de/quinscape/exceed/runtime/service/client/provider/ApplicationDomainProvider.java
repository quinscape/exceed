package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

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
        final DomainService domainService = runtimeContext.getDomainService();

        final HashMap<Object, Object> map = new HashMap<>();
        map.put("enumTypes", domainService.getEnums());
        map.put("domainTypes", domainService.getDomainTypes());
        return new DefaultClientData(map);
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
