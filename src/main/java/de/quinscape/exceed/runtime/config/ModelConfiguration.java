package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.action2.ActionService;
import de.quinscape.exceed.runtime.js.DefaultJsEnvironmentFactory;
import de.quinscape.exceed.runtime.js.ExpressionCompiler;
import de.quinscape.exceed.runtime.js.JsEnvironmentFactory;
import de.quinscape.exceed.runtime.model.ClientViewJSONGenerator;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.model.ModelLocationRule;
import de.quinscape.exceed.runtime.model.ModelLocationRules;
import de.quinscape.exceed.runtime.service.ActionExpressionRendererFactory;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ExpressionConfiguration.class)
public class ModelConfiguration
{
    private final static Logger log = LoggerFactory.getLogger(ModelConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ModelJSONService modelJSONService(ClientViewJSONGenerator clientViewJSONGenerator)
    {
        ModelJSONServiceImpl svc = new ModelJSONServiceImpl();
        svc.setClientViewJSONGenerator(clientViewJSONGenerator);

        return svc;
    }


    @Bean
    public ClientViewJSONGenerator clientViewJSONGenerator(ActionExpressionRendererFactory actionExpressionRendererFactory)
    {
        return new ClientViewJSONGenerator(actionExpressionRendererFactory);
    }

    @Bean
    public ActionExpressionRendererFactory actionExpressionRenderer(ActionService actionService)
    {
        return new ActionExpressionRendererFactory(actionService);
    }

    
    @Bean
    public ActionService actionService()
    {
        Map<String, Action> actions = findActionBeans();

        ClientSideOnlyAction clientSideOnlyAction = new ClientSideOnlyAction();
        actions.put("navigateTo", clientSideOnlyAction);

        return new DefaultActionService(actions);
    }


    private Map<String, Action> findActionBeans()
    {
        Map<String, Action> actions = new HashMap<>();

        for (Action action : applicationContext.getBeansOfType(Action.class).values())
        {
            // instantiate the action model per mandatory default constructor
            ActionModel actionModel = null;
            try
            {
                Class actionModelClass = action.getActionModelClass();
                if (actionModelClass == null)
                {
                    throw new IllegalStateException(action + " returns invalid action model class null");
                }
                actionModel = (ActionModel) actionModelClass.newInstance();
            }
            catch (Exception e)
            {
                throw new ExceedRuntimeException("Error creating empty action model for " + action, e);
            }
            // .. and register the action under the name provided by the model.
            actions.put(actionModel.getAction(), action);
        }
        return actions;
    }


    @Bean
    public ExpressionService expressionService()
    {
        HashSet<Object> operationBeans = new HashSet<>(applicationContext.getBeansWithAnnotation(ExpressionOperations
            .class).values());
        log.info("Operations in spring context: {}", operationBeans);

        QueryTransformerOperations queryTransformerOperations = new QueryTransformerOperations();
        operationBeans.add(queryTransformerOperations);
        operationBeans.add(new QueryFilterOperations());

        ExpressionServiceImpl svc = new ExpressionServiceImpl(operationBeans);

        queryTransformerOperations.setExpressionService(svc);

        return svc;
    }

    @Bean
    public ModelLocationRules modelLocationRules()
    {
        return new ModelLocationRules();
    }

    @Bean
    public ModelCompositionService modelCompositionService(ModelLocationRules modelLocationRules, ComponentRegistry componentRegistry)
    {
        return new ModelCompositionService(modelLocationRules, componentRegistry);
    }
}
