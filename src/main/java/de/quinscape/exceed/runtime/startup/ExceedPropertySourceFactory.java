package de.quinscape.exceed.runtime.startup;

import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.svenson.tokenize.InputStreamSource;

import java.io.IOException;

public class ExceedPropertySourceFactory
    implements PropertySourceFactory
{
    @Override
    public PropertySource<?> createPropertySource(
        String name, EncodedResource resource
    ) throws IOException
    {
        return new ExceedPropertySource(
            name,
            JSONUtil.DEFAULT_PARSER.parse(
                ExceedConfig.class,
                new InputStreamSource(resource.getInputStream(), true)
            )
        );
    }
}
