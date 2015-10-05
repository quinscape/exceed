package de.quinscape.exceed.runtime;

import de.quinscape.exceed.runtime.config.DomainConfiguration;
import de.quinscape.exceed.runtime.config.ModelConfiguration;
import de.quinscape.exceed.runtime.config.SecurityConfiguration;
import de.quinscape.exceed.runtime.config.WebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
    ModelConfiguration.class,
    DomainConfiguration.class,
    SecurityConfiguration.class,
    WebConfiguration.class
})
@Configuration
public class ExceedApplicationConfiguration
{

}
