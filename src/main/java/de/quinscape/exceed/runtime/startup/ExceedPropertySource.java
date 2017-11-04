package de.quinscape.exceed.runtime.startup;

import de.quinscape.exceed.model.startup.ExceedConfig;
import org.springframework.core.env.PropertySource;


public class ExceedPropertySource
    extends PropertySource<ExceedConfig>
{
    public final static String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    public ExceedPropertySource(String name, ExceedConfig source)
    {
        super(name, source);
    }

    @Override
    public Object getProperty(String name)
    {
        return getSource().getEnv().get(name);
    }
}
