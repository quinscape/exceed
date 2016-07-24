package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.message.Message;
import de.quinscape.exceed.runtime.controller.ExceedExceptionResolver;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.EditorWebSocketHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistryImpl;
import de.quinscape.exceed.runtime.service.websocket.WebSocketServerFactory;
import de.quinscape.exceed.runtime.util.MediaTypeService;
import de.quinscape.exceed.runtime.util.MediaTypeServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

@ComponentScan(
    value = {
        "de.quinscape.exceed.runtime.controller",
        "de.quinscape.exceed.runtime.editor"
    },
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EditorMessageHandler.class)
    }
)
@PropertySource({
    "/WEB-INF/profiles/${spring.profiles.active}.properties"
})
@Configuration
public class WebConfiguration
    extends WebMvcConfigurerAdapter
{
    private final static Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationContext applicationContext;

//    @Autowired
//    private ApplicationService applicationService;
//
//    @Bean
//    public JsService jsService() throws IOException
//    {
//        File sourceDir = Util.getExceedLibrarySource();
//        String sourceLocation = null;
//        ResourceRoot root;
//        if (sourceDir != null)
//        {
//            root = new FileResourceRoot(new File(sourceDir, Util.path("target/classes/META-INF/resources")), true);
//        }
//        else
//        {
//            root = new ServletResourceRoot(servletContext, "");
//        }
//        return new JsService(applicationService, root);
//
//    }
//


    @Override
    public void configureViewResolvers(ViewResolverRegistry registry)
    {
        // TODO: change this based on prod/dev
        servletContext.setAttribute("reactVersion", "react-0.14.7.js");
        servletContext.setAttribute("reactDOMVersion", "react-dom-0.14.7.js");

        registry.viewResolver(new ExceedViewResolver(applicationContext, servletContext));
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        //registry.addInterceptor(new CommonVariablesInterceptor(applicationContext, servletContext));
    }

    @Bean(name="simpleMappingExceptionResolver")
    public HandlerExceptionResolver exceedExceptionResolver()
    {
        return new ExceedExceptionResolver();
    }

    @Bean
    public MessageHubRegistry messageHubRegistry(EditorWebSocketHandler editorWebSocketHandler)
    {
        MessageHubRegistryImpl registry = new MessageHubRegistryImpl();
        registry.setEditorMessageHub(editorWebSocketHandler);
        return registry;
    }
    @Bean
    public MediaTypeService mediaTypeService()
    {
        return new MediaTypeServiceImpl(new ServletContextResource(servletContext, "/WEB-INF/mime.types"));
    }

    @Bean
    public EditorWebSocketHandler editorWebSocketHandler(ModelJSONService modelJSONService)
    {
        Map<String, EditorMessageHandler<? extends IncomingMessage>> handlers = new HashMap<>();

        for (EditorMessageHandler editorMessageHandler : applicationContext.getBeansOfType(EditorMessageHandler
            .class).values())
        {
            String name = Message.MESSAGE_PREFIX + editorMessageHandler.getMessageType().getSimpleName();
            EditorMessageHandler<? extends IncomingMessage> old = handlers.put(name, editorMessageHandler);

            if (old != null)
            {
                throw new IllegalStateException("Message name '" + name + "' is associated with both " + old + " and " + editorMessageHandler);
            }
        }

        return new EditorWebSocketHandler(handlers);
    }
    @Bean
    public WebSocketServerFactory webSocketServerFactory(EditorWebSocketHandler editorWebSocketHandler)
    {
        return new WebSocketServerFactory(editorWebSocketHandler);
    }
}
