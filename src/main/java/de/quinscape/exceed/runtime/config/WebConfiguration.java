package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.util.Util;
import de.quinscape.exceed.runtime.component.ComponentDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

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

    @Bean
    public JsService jsService() throws IOException
    {
        File sourceDir = Util.getExceedLibrarySource();
        String sourceLocation = null;
        if (sourceDir != null)
        {
            sourceLocation = new File(sourceDir, Util.path("target/classes/META-INF/resources/exceed/js/main.js")).getPath();
        }
        return new JsService(servletContext, sourceLocation);
    }

//    @Bean
//    public ViewResolver reactViewResolver() throws Exception
//    {
//        ReactViewResolver resolver = new ReactViewResolver();
//        resolver.setUseMinifiedReact(false);
//        resolver.setOrder(0);
//        resolver.setDevelopment(true);
//        return resolver;
//    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry)
    {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/exceed/views/");
        resolver.setSuffix(".jsp");
        resolver.setAlwaysInclude(false);

        // TODO: change this based on prod/dev
        servletContext.setAttribute("reactVersion", "react-with-addons.js");

        registry.viewResolver(resolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new CommonVariablesInterceptor());
    }

    @Bean
    public ComponentDataProvider defaultDataProvider()
    {
        return new ComponentDataProvider();
    }
}
