package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@Service
public class ApplicationDomainProvider
    implements RuntimeInfoProvider
{
    @Override
    public String getName()
    {
        return "applicationDomain";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        if (RequestUtil.isAjaxRequest(request))
        {
            return null;
        }

        final DomainService domainService = runtimeContext.getDomainService();

        final HashMap<Object, Object> map = new HashMap<>();
        map.put("enumTypes", domainService.getEnums());
        map.put("domainTypes", domainService.getDomainTypes());
        return map;
    }
}
