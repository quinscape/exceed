package de.quinscape.exceed.runtime.service.client.provider.docs;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedDocsProvider;
import de.quinscape.exceed.runtime.view.ViewData;
import de.quinscape.exceed.tooling.GenerateModelDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


@ExceedDocsProvider
public class ModelDocsProvider
    implements ClientStateProvider
{
    private final static Logger log = LoggerFactory.getLogger(ModelDocsProvider.class);

    public String getName()
    {
        return "modelDocs";
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
        return new DefaultClientData(new GenerateModelDocs().getModelDocs());
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
