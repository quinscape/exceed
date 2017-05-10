package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.model.JSONFormat;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

@ExceedAppProvider
public class ViewModelProvider
    implements ClientStateProvider
{
    private final ModelJSONService modelJSONService;


    @Autowired
    public ViewModelProvider(ModelJSONService modelJSONService)
    {
        this.modelJSONService = modelJSONService;
    }


    @Override
    public String getName()
    {
        return "model";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.VIEW;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
        Exception
    {
        return new JSONData(modelJSONService.toJSON(runtimeContext.getApplicationModel(), runtimeContext.getView(),
            JSONFormat.INTERNAL));
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
