package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.runtime.config.DefaultAppConfiguration;
import de.quinscape.exceed.runtime.config.DomainConfiguration;
import de.quinscape.exceed.runtime.config.EditorConfiguration;
import de.quinscape.exceed.runtime.config.ModelConfiguration;
import de.quinscape.exceed.runtime.config.SecurityConfiguration;
import de.quinscape.exceed.runtime.config.ServiceConfiguration;
import de.quinscape.exceed.runtime.config.WebConfiguration;
import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.runtime.startup.ExceedPropertySourceFactory;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.svenson.tokenize.InputStreamSource;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

import static de.quinscape.exceed.runtime.config.DefaultAppConfiguration.*;


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
@PropertySource(
    name = "ExceedConfig",
    factory = ExceedPropertySourceFactory.class,
    value = EXCEED_DEFAULT_CONFIG
)
public class ExceedApplicationConfiguration
{
    private final ServletContext servletContext;


    @Autowired
    public ExceedApplicationConfiguration(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    @Bean
    public ExceedConfig exceedConfig()
    {
        final ExceedConfig config = JSONUtil.DEFAULT_PARSER.parse(
            ExceedConfig.class,
            new InputStreamSource(
                servletContext.getResourceAsStream(EXCEED_DEFAULT_CONFIG),
                true
            )
        );

        final String basePath = getExtensionBasePath();

        config.setApps(
            copyWithBasePath(config.getApps(), basePath)
        );

        return config;
    }


    private List<AppState> copyWithBasePath(List<AppState> apps, String basePath)
    {
        List<AppState> list = new ArrayList<>(apps.size());
        for (AppState app : apps)
        {
            list.add(
                app.buildCopy()
                    .withPath(basePath)
                    .build()
            );
        }
        return list;
    }


    private String getExtensionBasePath()
    {
        final String configPath = servletContext.getRealPath(EXCEED_DEFAULT_CONFIG);
        final boolean runningFromFileSystem = configPath != null;

        String extensionPath;
        if (runningFromFileSystem)
        {
            extensionPath = "/WEB-INF/extensions";
        }
        else
        {
            extensionPath = "classpath:/WEB-INF/extensions";
        }

        return extensionPath;
    }

}
