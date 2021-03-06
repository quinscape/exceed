package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.message.Error;
import de.quinscape.exceed.message.Group;
import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.message.Message;
import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.model.AbstractModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSONParseException;
import org.svenson.JSONParser;
import org.svenson.matcher.OrMatcher;
import org.svenson.matcher.SubtypeMatcher;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EditorWebSocketHandler
    extends BaseWebSocketHandler
    implements EditorMessageHub, InitializingBean
{
    private static final String CONNECTION_ID_NAME = EditorWebSocketHandler.class.getName() + ":connectionId";

    private final static String GROUP_NAME = Message.MESSAGE_PREFIX + Group.class.getSimpleName();

    private final static Logger log = LoggerFactory.getLogger(EditorWebSocketHandler.class);


    private final ConcurrentMap<String, EditorClientConnection> connections;

    private final Map<String, EditorMessageHandler<? extends IncomingMessage>> handlers;

    private JSONParser parser;


    public EditorWebSocketHandler(
        Map<String,EditorMessageHandler<? extends IncomingMessage>> handlers
    )
    {
        this.handlers = handlers;
        connections = new ConcurrentHashMap<String, EditorClientConnection>();

    }


    @Override
    public void onOpen(WebSocketConnection connection) throws Exception
    {
        log.debug("open {}", connection);
    }


    public void send(String connectionId, Object message)
    {
        final EditorClientConnection connection = this.connections.get(connectionId);
        if (connection != null)
        {
            final DomainService domainService = connection.getRuntimeContext().getDomainService();
            WebSocketConnection webSocketConnection = connection.getWebSocketConnection();

            webSocketConnection.send(domainService.toJSON(message));
        }
    }


    @Override
    public void onMessage(WebSocketConnection webSocketConnection, final String msgIn) throws Exception
    {
        try
        {
            IncomingMessage msg = parser.parse(IncomingMessage.class, msgIn);
            String connectionId = msg.getMeta().getConnectionId();
            EditorClientConnection connection = connections.get(connectionId);
            if (connection == null)
            {
                final String errorJSON = JSONUtil.DEFAULT_GENERATOR.forValue(
                    new Error("Unregistered connection id '" +connectionId + "'.")
                );
                webSocketConnection.send(errorJSON);
                return;
            }

            if (connection.getWebSocketConnection() == null)
            {
                webSocketConnection.data(CONNECTION_ID_NAME, connectionId);
                connection.setWebSocketConnection(webSocketConnection);
            }

            if (msg instanceof Group)
            {
                handleGroup(connection, (Group) msg);
            }
            else
            {
                handleMessage(connection, msg);
            }
        }
        catch (JSONParseException e)
        {
            log.error("Error parsing '" + msgIn + "'.", e);
        }
    }


    private void handleGroup(EditorClientConnection connection, Group group) throws Exception
    {
        List<IncomingMessage> messages = group.getMessages();
        for (IncomingMessage msg : messages)
        {
            if (msg != null)
            {
                handleMessage(connection, msg);
            }
        }
    }


    private void handleMessage(MessageContext connection, IncomingMessage msg)
    {
        try
        {
            EditorMessageHandler<IncomingMessage> handler = (EditorMessageHandler<IncomingMessage>) handlers.get(msg.getType());

            if (handler == null)
            {
                throw new IllegalStateException("No handler for " + msg);
            }

            handler.handle(connection, msg);
        }
        catch (Exception e)
        {
            log.error("Error handling {}: {}", msg, e);

            if (msg instanceof Query)
            {
                connection.reply((Query) msg, new Error(e.getMessage()));
            }
        }
    }


    @Override
    public void onClose(WebSocketConnection connection) throws Exception
    {
        String id = (String) connection.data(CONNECTION_ID_NAME);
        if (id != null)
        {
            connections.remove(id);
        }
    }


    @Override
    public void register(String connectionId, AppAuthentication auth, HttpSession session, RuntimeContext
        runtimeContext)
    {
        log.debug("Register connectionId {}, login = {}", connectionId, auth);
        connections.put(connectionId, new EditorClientConnection(connectionId, auth, session, this,
            runtimeContext));
    }


    @Override
    public void afterPropertiesSet() throws Exception
    {
        parser = new JSONParser();
        parser.setTypeMapper(new MessageAndModelMapper());
    }


    public class MessageAndModelMapper
        extends AbstractPropertyValueBasedTypeMapper
    {

        public MessageAndModelMapper()
        {
            setDiscriminatorField("type");
            setPathMatcher(new OrMatcher(new SubtypeMatcher(AbstractModel.class), new SubtypeMatcher(Message.class)));
        }


        @Override
        protected Class getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            if (!(o instanceof String))
            {
                return null;
            }

            String name = (String)o;
            if (name.startsWith(Message.MESSAGE_PREFIX))
            {
                if (name.equals(GROUP_NAME))
                {
                    return Group.class;
                }

                EditorMessageHandler<? extends IncomingMessage> editorMessageHandler = handlers.get(name);
                if (editorMessageHandler == null)
                {
                    throw new IllegalStateException("No handler for'" + name + "'");
                }

                return editorMessageHandler.getMessageType();
            }

            return Model.getType((String)o);
        }
    }
}
