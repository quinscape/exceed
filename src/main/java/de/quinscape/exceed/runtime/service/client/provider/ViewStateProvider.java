package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;

@ExceedAppProvider
public class ViewStateProvider
    implements ClientStateProvider
{
    @Override
    public String getName()
    {
        return "viewState";
    }

    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.VIEW;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        View view = runtimeContext.getView();
        if (view != null)
        {
            String processName = view.getProcessName();
            if (processName != null)
            {
                Process process = runtimeContext.getApplicationModel().getProcess(processName);

                String localName = view.getLocalName();
                ProcessState state = process.getStates().get(localName);
                if (state instanceof ViewState)
                {
                    return new DefaultClientData(state);
                }
            }
        }
        return DefaultClientData.NONE;
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }

}
