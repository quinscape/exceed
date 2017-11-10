package de.quinscape.exceed.model.config;

import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.merge.MergeType;
import de.quinscape.exceed.runtime.universal.ServerRenderingStrategy;
import org.svenson.JSONProperty;

/**
 * Contains component configurations for the components within an exceed applications. Is embedded in
 * {@link ApplicationConfig}
 * <p>
 * This is were app-level configuration for components is stored. Is provided
 * </p>
 */
@MergeStrategy(MergeType.DEEP)
public class ComponentConfig
{

    /**
     * Whether input fields should propagate their values immediately as the user types to the context scopes etc or
     * if it should only validate the field but wait with propagating the value until the user leaves the field.
     * <p>
     *     Default is <code>true</code>
     * </p>
     */
    private boolean instantTextFields = true;

    private BaseTemplateConfig baseTemplateConfig;

    private ServerRenderingMode serverRenderingMode = ServerRenderingMode.DISABLED;

    private String serverRenderingStrategyName;


    public boolean isInstantTextFields()
    {
        return instantTextFields;
    }


    public void setInstantTextFields(boolean instantTextFields)
    {
        this.instantTextFields = instantTextFields;
    }

    @JSONProperty("baseTemplate")
    public BaseTemplateConfig getBaseTemplateConfig()
    {
        return baseTemplateConfig;
    }


    public void setBaseTemplateConfig(BaseTemplateConfig baseTemplateConfig)
    {
        this.baseTemplateConfig = baseTemplateConfig;
    }


    /**
     * Controls the server rendering mode for the application. Exceed can do a server-side reactjs prerendering
     * of the initial view for better SEO integration and faster UI. The way our js integration works however, we have
     * one server js environment per HTTP connection. Configuring the setting to {@link ServerRenderingMode#DISABLED} will
     * disabling the loading of the server render js bundle.
     *
     * @return mode enum
     */
    public ServerRenderingMode getServerRenderingMode()
    {
        return serverRenderingMode;
    }


    @JSONProperty("serverRendering")
    public void setServerRenderingMode(ServerRenderingMode serverRenderingMode)
    {
        this.serverRenderingMode = serverRenderingMode;
    }


    /**
     * Spring bean name of a {@link ServerRenderingStrategy} implementation to control whether or not the user receives
     * a pre-rendered initial document. Must be used in combination with serverRenderingMode {@link ServerRenderingMode#PER_STRATEGY}
     *
     * @return
     */
    public String getServerRenderingStrategyName()
    {
        return serverRenderingStrategyName;
    }

    @JSONProperty("serverRenderingStrategy")
    public void setServerRenderingStrategyName(String serverRenderingStrategyName)
    {
        this.serverRenderingStrategyName = serverRenderingStrategyName;
    }

}
