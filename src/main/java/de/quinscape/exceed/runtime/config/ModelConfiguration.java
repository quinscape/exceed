package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.model.ModelFactory;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.util.MediaTypeService;
import de.quinscape.exceed.runtime.util.MediaTypeServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.ServletContextResource;

@Configuration
public class ModelConfiguration
{
    @Bean
    public ModelJSONService modelJSONService()
    {
        return new ModelJSONServiceImpl(modelFactory());
    }

    @Bean
    public ModelFactory modelFactory()
    {
        return new ModelFactory();
    }
}
