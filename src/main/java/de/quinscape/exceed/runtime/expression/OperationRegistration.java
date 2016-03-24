package de.quinscape.exceed.runtime.expression;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;

public final class OperationRegistration
{
    private final MethodAccess access;

    private final int index;

    private final Object bean;

    private final Class<?>[] parameterTypes;

    private final String desc;

    private final Class<?> contextClass;


    public OperationRegistration(Object bean, MethodAccess access, int index, Class<?>[] parameterTypes, Class<?>
        contextClass, String desc)
    {
        this.bean = bean;
        this.access = access;
        this.index = index;
        this.parameterTypes = parameterTypes;
        this.contextClass = contextClass;
        this.desc = desc;
    }


    public Object invoke(ExpressionContext ctx, ASTFunction node, Object context)
    {
        int length = parameterTypes.length;
        Object[] params = new Object[length];

        if (contextClass == null)
        {
            if (context != null)
            {
                throw new IllegalArgumentException("Context must be null for " + desc);
            }
        }
        else
        {
            if (!contextClass.isInstance(context))
            {
                throw new IllegalArgumentException("Context is not assignable to " + contextClass + ": " + desc);
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

        return access.invoke(bean, index, params);
    }
}
