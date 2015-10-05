package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.model.ModelService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfiguration
{
    @Bean
    public ModelService modelService()
    {
        return new ModelService();
    }
}
