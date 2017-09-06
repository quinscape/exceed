package de.quinscape.exceed.runtime;

import de.quinscape.exceed.runtime.config.DefaultAppConfiguration;
import de.quinscape.exceed.runtime.config.DomainConfiguration;
import de.quinscape.exceed.runtime.config.EditorConfiguration;
import de.quinscape.exceed.runtime.config.ModelConfiguration;
import de.quinscape.exceed.runtime.config.SecurityConfiguration;
import de.quinscape.exceed.runtime.config.ServiceConfiguration;
import de.quinscape.exceed.runtime.config.WebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;


/**
 * Default configuration setup for exceed applications.
 */
@Import({
    ModelConfiguration.class,
    DomainConfiguration.class,
    SecurityConfiguration.class,
    ServiceConfiguration.class,
    EditorConfiguration.class,
    WebConfiguration.class,
    DefaultAppConfiguration.class
})
@Configuration
@PropertySource({
    "/WEB-INF/cfg/exceed-app.properties"
})
public class ExceedApplicationConfiguration
{

}
