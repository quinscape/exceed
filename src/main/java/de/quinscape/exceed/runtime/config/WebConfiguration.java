package de.quinscape.exceed.runtime.config;

import de.quinscape.dss.functions.AlphaHexColorFunction;
import de.quinscape.dss.functions.AverageColorOfImageFunction;
import de.quinscape.dss.functions.BestContrastFunction;
import de.quinscape.dss.functions.FloorFunction;
import de.quinscape.dss.functions.HSBAddFunction;
import de.quinscape.dss.functions.IfFunction;
import de.quinscape.dss.functions.LiteralFunction;
import de.quinscape.dss.functions.LogFunction;
import de.quinscape.dss.functions.MaxFunction;
import de.quinscape.dss.functions.MinFunction;
import de.quinscape.dss.functions.MixColorFunction;
import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.message.Message;
import de.quinscape.exceed.runtime.controller.ExceedExceptionResolver;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.EditorWebSocketHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistryImpl;
import de.quinscape.exceed.runtime.service.websocket.WebSocketServerFactory;
import de.quinscape.exceed.runtime.spring.ExceedViewResolver;
import de.quinscape.exceed.runtime.template.TemplateVariablesProvider;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.ServletContext;
import java.util.Collection;
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
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry)
    {
        final Collection<TemplateVariablesProvider> providers = applicationContext.getBeansOfType(TemplateVariablesProvider.class).values();

        log.info("Create view resolver using template variable providers: {}", providers);

        registry.viewResolver(new ExceedViewResolver(applicationContext, servletContext, providers));
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


    @Bean
    public MixColorFunction dss_mix()
    {
        return new MixColorFunction();
    }


    @Bean
    public BestContrastFunction dss_bestContrast()
    {
        return new BestContrastFunction();
    }


    @Bean
    public IfFunction dss_if()
    {
        return new IfFunction();
    }


    @Bean
    public LiteralFunction literal()
    {
        return new LiteralFunction();
    }


    @Bean
    public LogFunction dss_log()
    {
        return new LogFunction();
    }


    @Bean
    public LogFunction dss_error()
    {
        return new LogFunction();
    }


    @Bean
    public FloorFunction dss_floor()
    {
        return new FloorFunction();
    }


    @Bean
    public MinFunction dss_min()
    {
        return new MinFunction();
    }


    @Bean
    public MaxFunction dss_max()
    {
        return new MaxFunction();
    }


    @Bean
    public AverageColorOfImageFunction dss_avgColor()
    {
        return new AverageColorOfImageFunction();
    }


    @Bean
    public HSBAddFunction dss_hsbAdd()
    {
        return new HSBAddFunction();
    }


    @Bean
    public AlphaHexColorFunction dss_alphaHex()
    {
        return new AlphaHexColorFunction();
    }
}
