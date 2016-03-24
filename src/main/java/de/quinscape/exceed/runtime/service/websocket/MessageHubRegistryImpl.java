package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Handles registration for the actual MessageHub. Only registered clients are allowed to communicate with us.
 */
public class MessageHubRegistryImpl
    implements MessageHubRegistry
{
    private static Logger log = LoggerFactory.getLogger(MessageHubRegistryImpl.class);
    private EditorMessageHub editorMessageHub;

    @Required
    public void setEditorMessageHub(EditorMessageHub editorMessageHub)
    {
        this.editorMessageHub = editorMessageHub;
    }

    @Override
    public String registerConnection(HttpSession session, RuntimeContext runtimeContext, AppAuthentication auth)
    {
        String connectionId = UUID.randomUUID().toString();

        editorMessageHub.register(connectionId, auth, session, runtimeContext);

        if (log.isDebugEnabled())
        {
            log.debug("Register connectionId {} with auth {} and session {}", new Object[] { connectionId, auth, session.getId() });
        }

        return connectionId;
    }
}
