package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@ExceedAppProvider
public class ActionNamesProvider
    implements ClientStateProvider
{
    private final ActionService actionService;


    @Autowired
    public ActionNamesProvider(ActionService actionService)
    {
        this.actionService = actionService;
    }


    @Override
    public String getName()
    {
        return "actionNames";
    }

    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.APPLICATION;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        return new DefaultClientData(actionService.getActionNames());
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
