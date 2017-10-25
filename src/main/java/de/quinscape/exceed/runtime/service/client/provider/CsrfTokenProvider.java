package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.security.web.csrf.CsrfToken;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides the spring security token to the client side.
 * 
 */
@ExceedAppProvider
@ExceedEditorProvider
public class CsrfTokenProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "token";
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
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws Exception
    {
        final CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        return new JSONData(
            JSONBuilder.buildObject()
                .property("value", token.getToken())
                .property("header", token.getHeaderName())
                .property("param", token.getParameterName())
                .output()
        );
    }
}
