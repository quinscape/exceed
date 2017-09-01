package de.quinscape.exceed.runtime.action;

import java.lang.annotation.Annotation;

public final class ActionUtil
{
    private ActionUtil()
    {

    }

    public static <T extends Annotation> T find(Annotation[] annotations, Class<T> cls)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().equals(cls))
            {
                return (T) annotation;
            }
        }
        return null;
    }
}
