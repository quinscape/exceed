package de.quinscape.exceed.runtime.expression;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.expression.annotation.OperationParam;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public final class OperationRegistration
{
    private final MethodAccess access;

    private final int index;

    private final Object bean;

    private final Class<?>[] parameterTypes;

    private final String description;

    private final Class<?> returnType;

    private final OperationParam[] params;

    private final Class<?> contextClass;


    public OperationRegistration(
        Object bean,
        MethodAccess access,
        int index,
        Class<?>[] parameterTypes,
        Class<?> contextClass,
        String description,
        Class<?> returnType,
        OperationParam[] params
    )
    {
        this.bean = bean;
        this.access = access;
        this.index = index;
        this.parameterTypes = parameterTypes;
        this.contextClass = contextClass;
        this.description = description;
        this.returnType = returnType;
        this.params = params;
    }


    public Object invoke(ExpressionContext ctx, ASTFunction node, Object context)
    {
        try
        {
            Object[] params = prepareParameters(ctx, node, context);
            return access.invoke(bean, index, params);
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException(
                "Error invoking operation: " +
                    ExpressionUtil.renderExpressionOf(node) +
                    ": context = " + context +
                    ", env = " + ctx.getEnv() +
                    ", method =" + bean.getClass().getName() + "." + description, e
            );
        }
    }


    private Object[] prepareParameters(ExpressionContext ctx, ASTFunction node, Object context)
    {
        int length = parameterTypes.length;
        Object[] params = new Object[length];

        if (contextClass == null)
        {
            if (context != null)
            {
                throw new IllegalArgumentException("Context must be null for " + description);
            }
        }
        else
        {
            if (!contextClass.isInstance(context))
            {
                throw new IllegalArgumentException("Context is not assignable to " + contextClass + ": " + bean.getClass().getName() + "." + description);
            }
        }

        params[0] = ctx;

        int paramOffset = 1;
        if (parameterTypes.length > 1 && parameterTypes[1].isInstance(context))
        {
            params[1] = context;
            paramOffset++;
        }

        for (int i = 0; i < node.jjtGetNumChildren() && i + paramOffset < parameterTypes.length; i++)
        {
            Node n = node.jjtGetChild(i);
            Object param = n.jjtAccept(ctx.getEnv(), null);

//            Class<?> parameterType = parameterTypes[i + paramOffset];
//            if (param != null && !param.getClass().isPrimitive() && !parameterType.isAssignableFrom(param.getClass()))
//            {
//                throw new IllegalArgumentException("Argument type mismatch: " + desc + ", Argument #" + i + ": " +
//                    param.getClass() + " is not assignable to " + parameterType);
//            }
            params[i + paramOffset] = param;
        }
        return params;
    }


    public Object getBean()
    {
        return bean;
    }


    public String getTypeDescription()
    {
        return description;
    }


    public Class<?>[] getParameterTypes()
    {
        return parameterTypes;
    }


    public Class<?> getReturnType()
    {
        return returnType;
    }


    public OperationParam[] getOperationParams()
    {
        return params;
    }
}
