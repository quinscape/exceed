package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.message.Reply;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import org.springframework.web.util.WebUtils;
import org.webbitserver.WebSocketConnection;

import javax.servlet.http.HttpSession;

class EditorClientConnection
    implements MessageContext
{
    private final String connectionId;
    private final AppAuthentication login;
    private final HttpSession session;

    private final EditorWebSocketHandler editorWebSocketHandler;

    private final RuntimeContext runtimeContext;

    private WebSocketConnection webSocketConnection;

    public EditorClientConnection(String connectionId, AppAuthentication login, HttpSession session,
                                  EditorWebSocketHandler editorWebSocketHandler, RuntimeContext runtimeContext)
    {
        this.connectionId = connectionId;
        this.login = login;
        this.session = session;
        this.editorWebSocketHandler = editorWebSocketHandler;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public String getConnectionId()
    {
        return connectionId;
    }

    @Override
    public AppAuthentication getLogin()
    {
        return login;
    }


    @Override
    public void reply(Query msg, Object response)
    {
        MessageMeta meta = msg.getMeta();
        if (meta == null || meta.getMessageId() == null)
        {
            throw new IllegalStateException("Cannot respond to message without meta / message count");
        }
        editorWebSocketHandler.send(connectionId, new Reply(meta.getMessageId(), response));
    }


    @Override
    public Object getSessionAttribute(String name)
    {
        synchronized (WebUtils.getSessionMutex(this.session))
        {
            return this.session.getAttribute(name);
        }
    }

    @Override
    public void setSessionAttribute(String name, Object value)
    {
        synchronized (WebUtils.getSessionMutex(this.session))
        {
            this.session.setAttribute(name, value);
        }
    }

    public void setWebSocketConnection(WebSocketConnection webSocketConnection) {
        this.webSocketConnection = webSocketConnection;
    }

    public WebSocketConnection getWebSocketConnection() {
        return webSocketConnection;
    }


    @Override
    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }
}
