package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.view.ViewData;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides information about the current authentication (userName, roles).
 * 
 */
@ExceedAppProvider
@ExceedEditorProvider
public class AuthInfoProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "authentication";
    }

    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.USER;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws Exception
    {
        final AppAuthentication auth = runtimeContext.getAuthentication();

        return new JSONData(
            JSONBuilder.buildObject()
                .property("userName", auth.getUserName())
                .property("roles", auth.roles())
                .output()
        );
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
