package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class LocationInfoProvider
    implements RuntimeInfoProvider
{
    @Override
    public String getName()
    {
        return "location";
    }

    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        return  new Info(runtimeContext.getRoutingTemplate(),  runtimeContext.getLocationParams());
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
