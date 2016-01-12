package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.util.MediaTypeService;
import de.quinscape.exceed.runtime.util.MediaTypeServiceImpl;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;

@ComponentScan({
    "de.quinscape.exceed.runtime.controller"
})
@PropertySource({
    "/WEB-INF/profiles/${spring.profiles.active}.properties"
})
@Configuration
public class WebConfiguration
    extends WebMvcConfigurerAdapter
{
    private static Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Autowired
    private ServletContext servletContext;

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
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/exceed/views/");
        resolver.setSuffix(".jsp");
        resolver.setAlwaysInclude(false);

        // TODO: change this based on prod/dev
        servletContext.setAttribute("reactVersion", "react-0.14.6.js");
        servletContext.setAttribute("reactDOMVersion", "react-dom-0.14.6.js");

        registry.viewResolver(resolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new CommonVariablesInterceptor());
    }

    @Bean
    public MediaTypeService mediaTypeService()
    {
        return new MediaTypeServiceImpl(new ServletContextResource(servletContext, "/WEB-INF/mime.types"));
    }
}
