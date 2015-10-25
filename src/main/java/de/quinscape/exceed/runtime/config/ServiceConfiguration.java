package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
    "de.quinscape.exceed.runtime.service"
})
public class ServiceConfiguration
{
    @Bean
    public ViewDataService viewDataService()
    {
        return new ViewDataService();
    }

    @Bean
    public Translator translator()
    {
        return new DefaultTranslator();
    }
}
