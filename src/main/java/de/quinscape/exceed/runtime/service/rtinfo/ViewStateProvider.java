package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class ViewStateProvider
    implements RuntimeInfoProvider
{
    @Override
    public String getName()
    {
        return "viewState";
    }

    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
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
                    return state;
                }
            }
        }
        return null;
    }

}
