package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.RuntimeContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class LocationParamsProvider
    implements RuntimeInfoProvider
{
    @Override
    public String getName()
    {
        return "locationParams";
    }

    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        return runtimeContext.getLocationParams();
    }
}
