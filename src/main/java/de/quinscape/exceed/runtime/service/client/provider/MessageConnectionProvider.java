package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.RequestAttributes;
import de.quinscape.exceed.runtime.security.Roles;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.view.ViewData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@ExceedAppProvider
@ExceedEditorProvider
public class MessageConnectionProvider
    implements ClientStateProvider
{
    private final static Logger log = LoggerFactory.getLogger(MessageConnectionProvider.class);

    private MessageHubRegistry messageHubRegistry;


    @Autowired(required = false)
    public void setMessageHubRegistry(MessageHubRegistry messageHubRegistry)
    {
        this.messageHubRegistry = messageHubRegistry;
    }


    @Override
    public String getName()
    {
        return "connectionId";
    }


    @Override
    public boolean isSkippedOnAjax()
    {
        // connection is established with bootup request and not updated on AJAX
        return true;
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.REQUEST;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
        Exception
    {
        final AppAuthentication auth = runtimeContext.getAuthentication();

        final Object connectionId;
        if (auth.hasRole(Roles.EDITOR) && !RequestUtil.isAjaxRequest(request))
        {
            connectionId = messageHubRegistry.registerConnection(request.getSession(), runtimeContext, auth);
        }
        else if (messageHubRegistry == null)
        {
            log.info("No messageHubRegistry bean defined");
            connectionId = false;
        }
        else
        {
            connectionId = null;
        }

        return new DefaultClientData(connectionId);
    }
}
