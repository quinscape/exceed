package de.quinscape.exceed.runtime.action;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionNotFoundException;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.FunctionDefinition;
import de.quinscape.exceed.runtime.util.Util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultActionService
    implements ActionService
{
    public static final String CHAPTER_ACTION = "Action expressions";

    private final Map<String, ActionRegistration> actions;

    private final Map<String, ActionRegistration> actionsRO;


    public DefaultActionService(
        Collection<?> actionComponents,
        List<ParameterProviderFactory> parameterProviderFactories
    )
    {
        actions = new HashMap<>();
        actionsRO = Collections.unmodifiableMap(actions);

        analyze(actionComponents, parameterProviderFactories);
    }


    private void analyze(Collection<?> actionComponents, List<ParameterProviderFactory> parameterProviderFactories)
    {
        for (Object component : actionComponents)
        {
            if (component != null)
            {
                analyzeComponent(parameterProviderFactories, component);
            }
        }
    }


    private void analyzeComponent(List<ParameterProviderFactory> parameterProviderFactories, Object component)
    {
        final MethodAccess componentAccess = MethodAccess.get(component.getClass());

        for (Method m : component.getClass().getMethods())
        {
            final Action actionAnno = m.getAnnotation(Action.class);
            if (actionAnno != null)
            {
                final String nameFromAnnotation = actionAnno.value();
                final String actionName = nameFromAnnotation.equals(Action.METHOD_NAME) ? m.getName() : nameFromAnnotation;

                actions.put(actionName,
                    new MethodAccessRegistration(
                        actionName,
                        component,
                        componentAccess,
                        m,
                        parameterProviderFactories,
                        actionAnno.env(),
                        Util.join(
                            Arrays.asList(
                                actionAnno.description()
                            ),
                            "\n"
                        )
                    )
                );
            }
        }
    }


    @Override
    public ActionResult execute(RuntimeContext runtimeContext, String action, ActionParameters params)
    {
        final ActionRegistration actionRegistration = actions.get(action);

        if (actionRegistration == null)
        {
            throw new ActionNotFoundException("Action " + action + " not found");
        }

        return actionRegistration.execute(runtimeContext, params);
    }


    public Map<String, ActionRegistration> getRegistrations()
    {
        return actionsRO;
    }


    @Override
    public Definitions getActionFunctionDefinitions()
    {
        final Definitions functionDefinitions = new Definitions();

        for (Map.Entry<String, ActionRegistration> entry : actions.entrySet())
        {
            final String name = entry.getKey();
            final ActionRegistration registration = entry.getValue();
            
            functionDefinitions.addDefinition(
                name,
                new FunctionDefinition(
                    name,
                    registration.getReturnType(),
                    registration.getDescription(),
                    ExpressionType.ACTION,
                    registration.getParameterModels(),
                    null,
                    registration.isVarArgs(),
                    registration.isServerSide() ? DefinitionType.ACTION : DefinitionType.CLIENT_SIDE_ACTION,
                    CHAPTER_ACTION
                )
            );
        }

        return functionDefinitions;
    }
}
