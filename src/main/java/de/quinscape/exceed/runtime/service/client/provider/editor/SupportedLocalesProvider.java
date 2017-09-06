package de.quinscape.exceed.runtime.service.client.provider.editor;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;

@ExceedEditorProvider
public class SupportedLocalesProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "supportedLocales";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.APPLICATION;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
        Exception
    {
        return new DefaultClientData(runtimeContext.getApplicationModel().getConfigModel().getSupportedLocales());
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
