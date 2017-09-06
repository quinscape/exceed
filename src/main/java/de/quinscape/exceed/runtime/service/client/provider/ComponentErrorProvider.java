package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.QueryError;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.view.ViewData;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@ExceedAppProvider
public class ComponentErrorProvider
    implements ClientStateProvider
{

    @Override
    public String getName()
    {
        return "componentError";
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
        throws Exception
    {
        final Map<String, List<QueryError>> errors = viewData.getErrors();

        final int count = errors != null ? errors.size() : 0;

        return new JSONData(
            JSONBuilder.buildObject()
                .property("count", count)
                .property("errors", errors)
                .output()
        );
    }
}
