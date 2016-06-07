package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@Service
public class EnumValuesProvider
    implements RuntimeInfoProvider
{
    @Override
    public String getName()
    {
        return "enums";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        if (RequestUtil.isAjaxRequest(request))
        {
            return null;
        }

        return runtimeContext.getDomainService().getEnums();
    }
}
