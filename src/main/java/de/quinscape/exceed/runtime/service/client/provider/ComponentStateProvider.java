package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.view.ComponentData;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ExceedAppProvider
public class ComponentStateProvider
    implements ClientStateProvider
{

    @Override
    public String getName()
    {
        return "component";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.REQUEST;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
        throws Exception
    {
        final Map<String, ComponentData> componentData = viewData.getComponentData();
        return new JSONData(runtimeContext.getDomainService().toJSON(componentData));
    }


    @Override
    public boolean isMutable()
    {
        return true;
    }
}
