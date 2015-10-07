package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.model.ModelFactory;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public ResourceLoader resourceLoader()
    {
        return new ResourceLoader();
    }
}
