package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.js.ExpressionCompiler;
import de.quinscape.exceed.runtime.js.JsEnvironmentFactory;
import de.quinscape.exceed.runtime.model.ClientViewJSONGenerator;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.model.ModelLocationRules;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ExpressionConfiguration.class)
public class ModelConfiguration
{
    public final static String MERGE_REPLACE_STRATEGY_NAME = "mergeReplaceStrategy";
    public final static String MERGE_DEEP_STRATEGY_NAME = "mergeDeepStrategy";

    @Bean
    public ModelJSONService modelJSONService(ClientViewJSONGenerator clientViewJSONGenerator)
    {
        ModelJSONServiceImpl svc = new ModelJSONServiceImpl();
        svc.setClientViewJSONGenerator(clientViewJSONGenerator);
        return svc;
    }

    @Bean
    public ClientViewJSONGenerator clientViewJSONGenerator(ActionService actionService)
    {
        return new ClientViewJSONGenerator(actionService);
    }

    @Bean
    public ModelLocationRules modelLocationRules()
    {
        return new ModelLocationRules();
    }

    @Bean
    public ModelCompositionService modelCompositionService(
        ModelLocationRules modelLocationRules,
        ComponentRegistry componentRegistry,
        ModelSchemaService modelSchemaService,
        ModelJSONService modelJSONService,
        JsEnvironmentFactory jsEnvironmentFactory,
        ApplicationContext applicationContext
    )
    {
        return new ModelCompositionService(
            modelLocationRules,
            modelSchemaService,
            componentRegistry,
            modelJSONService,
            jsEnvironmentFactory,
            applicationContext
        );
    }

    @Bean
    public JsEnvironmentFactory jsEnvironmentFactory(
        ActionService actionService,
        NashornScriptEngine nashorn,
        ExpressionCompiler compiler
    )
    {
        return new JsEnvironmentFactory(
            actionService,
            nashorn,
            compiler
        );
    }

}
