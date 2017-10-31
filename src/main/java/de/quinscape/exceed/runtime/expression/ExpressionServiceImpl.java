package de.quinscape.exceed.runtime.expression;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Identifier;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExpressionServiceImpl
    implements ExpressionService, OperationService
{
    private final static Logger log = LoggerFactory.getLogger(ExpressionServiceImpl.class);

    private final Map<OperationKey, OperationRegistration>   operationLookup;
    private final Map<IdentifierKey, IdentifierRegistration> identifierLookup;


    public ExpressionServiceImpl(Collection<Object> operationBeans)
    {
        operationLookup = new HashMap<>();
        identifierLookup = new HashMap<>();

        for (Object bean : operationBeans)
        {
            log.info("Registering {} as operations bean", bean);

            ExpressionOperations anno = bean.getClass().getAnnotation(ExpressionOperations.class);
            if (anno == null)
            {
                throw new IllegalStateException("Operations bean " + bean + " has no @ExpressionOperations annotation");
            }

            Class<? extends ExpressionEnvironment> cls = anno.environment();
            registerOperationsAndIdentifiers(bean, cls);
        }
    }

    private void registerOperationsAndIdentifiers(Object bean, Class<? extends ExpressionEnvironment> envClass)
    {
        MethodAccess access = MethodAccess.get(bean.getClass());

        for (Method m : bean.getClass().getMethods())
        {
            Operation anno = m.getAnnotation(Operation.class);
            final String methodName = m.getName();
            if (anno != null)
            {
                Class<?> context = anno.context().equals(void.class) ? null : anno.context();
                final String operationName = anno.name().length() > 0 ? anno.name() : methodName;

                OperationKey key = new OperationKey(envClass, context, operationName);

                Class<?>[] parameterTypes = m.getParameterTypes();

                int paramCount = parameterTypes.length;
                if (paramCount == 0 || !parameterTypes[0].equals(ExpressionContext.class))
                {
                    throw new IllegalStateException("First @Operation method parameter must be " + ExpressionContext.class.getName() + " : " + m);
                }

                OperationRegistration registration = new OperationRegistration(
                    bean,
                    access,
                    access.getIndex(methodName, m.getParameterTypes()),
                    parameterTypes,
                    context,
                    renderMethodDesc(m),
                    m.getReturnType(),
                    anno.params()
                );
                operationLookup.put(
                    key,
                    registration
                );
                log.debug("Register operation {} => {}", key, registration);
            }

            Identifier identAnno = m.getAnnotation(Identifier.class);
            if (identAnno != null)
            {
                if (m.getReturnType().equals(void.class))
                {
                    throw new IllegalStateException("@Identifier methods must return values: " + m);
                }

                boolean takesEnvParam = m.getParameterTypes().length == 1 && ExpressionEnvironment.class.isAssignableFrom(m
                    .getParameterTypes()[0]);
                if (m.getParameterTypes().length > 0 && !takesEnvParam)
                {
                    throw new IllegalStateException("@Identifier methods can only have one ExpressionEnv parameter: " + m);
                }

                String identifierName = methodName;

                String nameFromAnnotation = identAnno.name();
                if (nameFromAnnotation.length() > 0)
                {
                    identifierName = nameFromAnnotation;
                }

                IdentifierKey key = new IdentifierKey(envClass, identifierName);
                IdentifierRegistration registration = new IdentifierRegistration(bean, access, access.getIndex(
                    methodName,
                    m.getParameterTypes()), takesEnvParam, m.toString());
                identifierLookup.put(key, registration);

                log.debug("Register identifier {} => {}", key, registration);
            }
        }
    }


    public static String renderMethodDesc(Method m)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName());
        sb.append("(");

        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            Class<?> cls = parameterTypes[i];
            sb.append(cls.getSimpleName());
        }
        sb.append(")");
        return sb.toString();
    }


    @Override
    public Object resolveIdentifier(ExpressionEnvironment env, String name)
    {
        IdentifierRegistration registration = identifierLookup.get(new IdentifierKey(env
            .getClass(), name));

        if (registration == null)
        {
            return env.resolveIdentifier(name);
        }
        return registration.get(env);
    }

    @Override
    public Object evaluate(ExpressionEnvironment expressionEnvironment, ASTFunction node, Object context)
    {
        final ExpressionContext<ExpressionEnvironment> ctx = new ExpressionContext<>(expressionEnvironment, node);
        final String operationName = node.getName();
        OperationRegistration registration = lookupRegistration(expressionEnvironment, context, operationName);
        if (registration == null)
        {
            return expressionEnvironment.undefinedOperation(node, context);
        }
        return registration.invoke(ctx, node, context);
    }


    private OperationRegistration lookupRegistration(
        ExpressionEnvironment expressionEnvironment, Object context, String operationName
    )
    {
        final OperationKey key = new OperationKey(
            expressionEnvironment.getClass(),
            context == null ? null : context.getClass(),
            operationName
        );

        return operationLookup.get(key);
    }


    @Override
    public Object evaluate(Node node, ExpressionEnvironment env)
    {
        if (env == null)
        {
            throw new IllegalArgumentException("env can't be null");
        }

        env.setOperationService(this);
        return node.jjtAccept(env, null);
    }


    public Map<OperationKey, OperationRegistration> getOperationLookup()
    {
        return operationLookup;
    }


    public Map<IdentifierKey, IdentifierRegistration> getIdentifierLookup()
    {
        return identifierLookup;
    }
}
