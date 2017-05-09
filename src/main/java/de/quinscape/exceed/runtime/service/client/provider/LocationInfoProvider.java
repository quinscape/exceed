package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ExceedAppProvider
public class LocationInfoProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "location";
    }

    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.REQUEST;
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        return new DefaultClientData(new Info(runtimeContext.getRoutingTemplate(),  runtimeContext.getLocationParams()));
    }

    public static class Info
    {
        private final String routingTemplate;
        private final Map<String,Object> params;


        public Info(String routingTemplate, Map<String, Object> params)
        {
            this.routingTemplate = routingTemplate;
            this.params = params;
        }


        public String getRoutingTemplate()
        {
            return routingTemplate;
        }


        public Map<String, Object> getParams()
        {
            return params;
        }
    }
}
