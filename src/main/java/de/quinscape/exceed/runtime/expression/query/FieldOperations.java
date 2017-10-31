package de.quinscape.exceed.runtime.expression.query;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Maps;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds the reflectively detected operations on JOOQ's {@link Field}.
 *
 */
public class FieldOperations
{
    private final static MethodAccess fieldAccess = MethodAccess.get(Field.class);

    private final static Map<String, Integer> operationAccessIndizies;
    private final static Map<String, Class<?>[]> operationParams;

    static
    {
        final Method[] methods = Field.class.getMethods();
        Map<String, Integer> operations = Maps.newHashMapWithExpectedSize(methods.length);
        Map<String, Class<?>[]> params = Maps.newHashMapWithExpectedSize(methods.length);
        for (Method m : methods)
        {
            if (m.getReturnType().equals(Condition.class) && (
                m.getParameterTypes().length == 0 ||
                m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(Field.class)
            )
            )
            {
                operations.put(m.getName(), fieldAccess.getIndex(m.getName(), m.getParameterTypes()));
                params.put(m.getName(), m.getParameterTypes());
            }
        }

        operationAccessIndizies = operations;
        operationParams = params;
    }

    public static Set<String> getOperationNames()
    {
        return operationAccessIndizies.keySet();
    }

    public static boolean contains(String name)
    {
        return operationAccessIndizies.get(name) != null;
    }

    public static Condition execute(String name, Field f, Object value)
    {
        if (!(value instanceof Field))
        {
            value = DSL.val(value);
        }

        Integer index = operationAccessIndizies.get(name);
        if (index == null)
        {
            throw new IllegalArgumentException("Invalid field operation: " + name);
        }
        return (Condition) fieldAccess.invoke(f, index, value);
    }


    public static Class<?>[] getParameterTypes(String operationName)
    {
        return operationParams.get(operationName);
    }
}
