package de.quinscape.exceed.runtime.action;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.exceed.model.annotation.ExceedPropertyType;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implements a Java method based action registration using ReflectASM
 */
public class MethodAccessRegistration
    implements ActionRegistration
{

    private final static Logger log = LoggerFactory.getLogger(MethodAccessRegistration.class);

    public final static DomainProperty VOID_TYPE = DomainProperty.builder().withType("Void").build();

    private final Object component;

    private final MethodAccess componentAccess;

    /**
     * Contains property models for all action parameters, i.e. the Java method parameters for which no {@link ParameterProvider}
     * was registered. This corresponds to the order and number of parameters on the js side of the action.
     */
    private final List<DomainProperty> actionParameterModels;

    /**
     * Parameter count on the Java side
     */
    private final int parameterCount;

    /**
     * Property type for the return type of this action
     */
    private final DomainProperty returnType;

    /**
     * ReflectASM index for the underlying java method
     */
    private final int actionMethodIndex;

    /**
     * List of parameter infos. One for each Java method parameter
     */
    private final List<ParameterInfo> parameterInfos;

    private final boolean varArgs;

    private final String actionName;

    private final Class<?> varArgsType;

    private final ActionEnvironment actionEnvironment;

    private final String description;


    private boolean typesInitialized;



    public DomainProperty getReturnType()
    {
        return returnType;
    }


    public List<DomainProperty> getParameterModels()
    {
        return actionParameterModels;
    }

    public MethodAccessRegistration(
        String actionName,
        Object component,
        MethodAccess componentAccess,
        Method method,
        Collection<ParameterProviderFactory> parameterProviderFactories,
        ActionEnvironment actionEnvironment, String description
    )
    {
        this.actionEnvironment = actionEnvironment;
        this.description = description;
        log.debug("MethodAccessRegistration: {}, {}, {}", actionName, component, method);

        this.actionName = actionName;
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Annotation[][] allParameterAnnotations = method.getParameterAnnotations();

        this.component = component;
        this.componentAccess = componentAccess;
        this.actionMethodIndex = this.componentAccess.getIndex(method.getName(), parameterTypes);

        parameterCount = parameterTypes.length;

        parameterInfos = new ArrayList<>(parameterCount);
        actionParameterModels = new ArrayList<>(parameterCount);

        final ExceedPropertyType methodTypeAnno = ActionUtil.find(method.getAnnotations(), ExceedPropertyType.class);
        if (methodTypeAnno != null)
        {
            this.returnType = DomainProperty.builder()
                .fromAnnotation(methodTypeAnno)
                .build();
        }
        else if (method.getReturnType().equals(void.class))
        {
            this.returnType = VOID_TYPE;
        }
        else
        {
            this.returnType = ExpressionUtil.getPropertyModelFor(method.getReturnType());
        }

        varArgs = method.isVarArgs();
        varArgsType = varArgs ? parameterTypes[parameterCount - 1].getComponentType() : null;

        int position = 0;
        final int last = parameterCount - 1;
        for (int i = 0; i < parameterCount; i++)
        {
            Class<?> paramType = parameterTypes[i];
            final Annotation[] parameterAnnotations = allParameterAnnotations[i];

            ParameterProvider provider = resolveProvider(parameterProviderFactories, paramType, parameterAnnotations);
            final ExceedPropertyType typeAnno = ActionUtil.find(parameterAnnotations, ExceedPropertyType.class);
            final boolean isRequired = typeAnno != null && typeAnno.required();

            if (provider == null)
            {
                final DomainProperty propertyModel;

                if (typeAnno != null)
                {
                    propertyModel = DomainProperty.builder()
                        .fromAnnotation(typeAnno)
                        .build();
                }
                else
                {
                    final Class<?> type;
                    if (varArgs && i == last)
                    {
                        type = paramType.getComponentType();
                    }
                    else
                    {
                        type = paramType;
                    }
                    propertyModel = ExpressionUtil.getPropertyModelFor(type);
                }

                actionParameterModels.add(propertyModel);

                this.parameterInfos.add(
                    new ParameterInfo(
                        position++,
                        isRequired
                    )
                );
            }
            else
            {
                this.parameterInfos.add(
                    new ParameterInfo(
                        provider,
                        isRequired
                    )
                );
            }
        }
    }


    private ParameterProvider resolveProvider(
        Collection<ParameterProviderFactory> parameterProviderFactories, Class<?> paramType,
        Annotation[] parameterAnnotations
    )
    {
        if (ActionExecution.class.isAssignableFrom(paramType))
        {
            return ActionExecutionPlaceholder.INSTANCE;
        }

        try
        {
            ParameterProvider provider = null;
            for (ParameterProviderFactory factory : parameterProviderFactories)
            {
                provider = factory.createIfApplicable(paramType, parameterAnnotations);
                if (provider != null)
                {
                    break;
                }
            }
            return provider;
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException("Error resolving action parameter provider", e);
        }
    }


    @Override
    public ActionResult execute(RuntimeContext runtimeContext, ActionParameters parameters) throws InvalidActionParameterException
    {
        if (!typesInitialized)
        {
            for (DomainProperty property : actionParameterModels)
            {
                PropertyType.get(runtimeContext, property);
            }
            typesInitialized = true;
        }

        final List<Object> positionalParameters = parameters.get(runtimeContext, this, actionParameterModels);

        final ActionExecution actionExecution = new DefaultActionExecution();

        Object[] callArgs = new Object[parameterCount];

        final int numberOfParameters;
        if (varArgs)
        {
            final int lastParam = actionParameterModels.size() - 1;
            final int actualParameterCount = positionalParameters.size();
            if (actualParameterCount >= lastParam)
            {
                final Object array = Array.newInstance(varArgsType, actualParameterCount - lastParam);
                callArgs[parameterCount - 1] = array;

                final int count = actualParameterCount - lastParam;
                for (int i = 0; i < count; i++)
                {
                    Array.set(array, i, positionalParameters.get(lastParam + i));
                }
            }
            numberOfParameters = parameterCount - 1;
        }
        else
        {
            numberOfParameters = parameterCount;
        }

        for (int i = 0; i < numberOfParameters; i++)
        {
            final ParameterInfo info = parameterInfos.get(i);
            callArgs[i] = info.provide(runtimeContext, positionalParameters, actionExecution);
        }


        final Object result = componentAccess.invoke(component, actionMethodIndex, callArgs);

        return new DefaultActionResult(runtimeContext, result, returnType, actionExecution.isResolved());
    }


    public String getActionName()
    {
        return actionName;
    }


    @Override
    public boolean isVarArgs()
    {
        return varArgs;
    }


    public boolean isServerSide()
    {
        return actionEnvironment != ActionEnvironment.CLIENT;
    }
    public boolean isClientSide()
    {
        return actionEnvironment != ActionEnvironment.SERVER;
    }


    @Override
    public String getDescription()
    {
        return description;
    }
}
