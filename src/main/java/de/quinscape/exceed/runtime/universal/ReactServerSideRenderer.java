package de.quinscape.exceed.runtime.universal;

import de.quinscape.exceed.model.config.ComponentConfig;
import de.quinscape.exceed.model.config.ServerRenderingMode;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import de.quinscape.exceed.runtime.template.TemplateVariablesProvider;

import java.util.Map;

/**
 * Prerenders the exceed view by invoking a server-render based on the {@link  TemplateVariables#VIEW_DATA} json provided
 * to the base template. The result is stored as prerendered {@link  TemplateVariables#CONTENT}.
 */
public class ReactServerSideRenderer
    implements TemplateVariablesProvider
{
    private final Map<String,ServerRenderingStrategy> strategies;


    public ReactServerSideRenderer(
        Map<String, ServerRenderingStrategy> strategies
    )
    {
        this.strategies = strategies;
    }


    @Override
    public void provide(
        RuntimeContext runtimeContext,
        Map<String, Object> model
    )
    {
        final ComponentConfig componentConfig = runtimeContext.getApplicationModel().getConfigModel()
            .getComponentConfig();

        final ServerRenderingMode serverRenderingMode = componentConfig.getServerRenderingMode();
        if (
            serverRenderingMode != ServerRenderingMode.PER_STRATEGY ||
            check(
                runtimeContext,
                componentConfig.getServerRenderingStrategyName()
            )
        )
        {
            final JsEnvironment env = runtimeContext.getJsEnvironment();
            final String content = env.renderToString(runtimeContext, (String) model.get(TemplateVariables.VIEW_DATA));
            model.put(TemplateVariables.CONTENT, content);
        }
    }

    private boolean check(RuntimeContext runtimeContext, String serverRenderingStrategyName)
    {
        final ServerRenderingStrategy strategy = strategies.get(serverRenderingStrategyName);
        if (strategy == null)
        {
            throw new IllegalStateException("No ServerRenderingStrategy with name '" + serverRenderingStrategyName + "' found");
        }

        return strategy.shouldDoServerSideRendering(runtimeContext);
    }
}
