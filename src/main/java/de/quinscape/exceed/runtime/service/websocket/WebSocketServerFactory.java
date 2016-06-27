package de.quinscape.exceed.runtime.service.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;

public class WebSocketServerFactory
    implements InitializingBean
{
    private final static Logger log = LoggerFactory.getLogger(WebSocketServerFactory.class);

    private WebServer webServer;

    private EditorWebSocketHandler editorWebSocketHandler;
    
    public WebSocketServerFactory(EditorWebSocketHandler editorWebSocketHandler)
    {
        this.editorWebSocketHandler = editorWebSocketHandler;
    }

    @PreDestroy
    public void destroy() throws ExecutionException, InterruptedException
    {
        log.info("Stopping webbit");
        webServer.stop().get();
    }


    @Override
    public void afterPropertiesSet() throws Exception
    {
        log.info("Creating web socket server");

        webServer = WebServers.createWebServer(9876)
        // .addElementDefinition(new LoggingHandler(new
        // SimpleLogSink(ApplicationWebSocketHandler.USERNAME_KEY)))
            .add("/appsocket", editorWebSocketHandler)
            // .addElementDefinition(new
            // StaticFileHandler("./src/test/java/samples/chatroom/content"))
            .start().get();

    }
}
