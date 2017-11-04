package de.quinscape.exceed.model.startup;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import org.svenson.JSONTypeHint;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Java container for the exceed app container configuration in <code></code>/WEB-INF/cfg/exceed-config.json</code>
 */
public class ExceedConfig
    extends AbstractTopLevelModel
{
    private Map<String, String> env;

    private List<AppState> apps;

    private String defaultApp;


    public void setEnv(Map<String, String> env)
    {
        this.env = env;
    }


    @JSONTypeHint(AppState.class)
    public void setApps(List<AppState> apps)
    {
        this.apps = apps;
    }


    public void setDefaultApp(String defaultApp)
    {
        this.defaultApp = defaultApp;
    }


    /**
     * Array of application definitions
     * @return
     */
    public List<AppState> getApps()
    {
        return apps;
    }


    /**
     * Default application for the exceed application container. Default is the first application.
     */
    public String getDefaultApp()
    {
        if (defaultApp == null)
        {
            return apps.get(0).getName();
        }

        return defaultApp;
    }


    /**
     * Env properties provided by the exceed-config.json
     *
     * @return
     */
    public Map<String, String> getEnv()
    {
        return env;
    }


    @Override
    public <I, O> O accept(TopLevelModelVisitor<I, O> visitor, I in)
    {
        // exceed-config.json is not part of the normal application runtime model
        throw new UnsupportedOperationException();
    }


    @PostConstruct
    public void validate()
    {
        if (apps == null || apps.size() == 0)
        {
            throw new IllegalArgumentException("apps can't be null or empty");
        }
    }
}
