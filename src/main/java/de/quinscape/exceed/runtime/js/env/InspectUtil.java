package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.runtime.util.JSONUtil;
import jdk.nashorn.api.scripting.JSObject;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public class InspectUtil
{
    public static String inspect(Object o)
    {
        final StringBuilder sb = new StringBuilder();
        if (o == null || o instanceof Number || o instanceof Boolean || o instanceof String)
        {
            sb.append(o);
        }
        else
        {
            dump(sb, o, new IdentityHashMap<Object, Boolean>(), 4);
        }

        return sb.toString();
    }


    private static String inspect(Object value, int maxDepth)
    {
        final StringBuilder sb = new StringBuilder();
        dump(sb, value, new IdentityHashMap<>(), maxDepth);
        return sb.toString();
    }
    
    private static void dump(StringBuilder sb, Object value, IdentityHashMap<Object, Boolean> visited, int maxDepth)
    {

        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof String)
        {
            sb.append(value);
        }
        else
        {
            if (maxDepth == 0)
            {
                sb.append("...");
                return;
            }

            if (visited.containsKey(value))
            {
                sb.append("<CYCLIC: ")
                    .append(value.getClass())
                    .append("@")
                    .append(Integer.toHexString(System.identityHashCode(value)))
                    .append(">");

            }
            else
            {
                visited.put(value, Boolean.TRUE);

                if (value.getClass().isArray())
                {
                    sb.append('[');
                    final int length = Array.getLength(value);
                    for (int i = 0; i < length; i++ )
                    {
                        if (i > 0)
                        {
                            sb.append(",");
                        }

                        dump(
                            sb,
                            Array.get(value, i),
                            visited,
                            maxDepth - 1
                        );
                    }
                    sb.append(']');
                }
                else if (value instanceof Collection)
                {
                    sb.append('[');
                    for (Iterator<?> iterator = ((Collection) value).iterator(); iterator.hasNext(); )
                    {
                        dump(
                            sb,
                            iterator.next(),
                            visited,
                            maxDepth - 1
                        );

                        if (iterator.hasNext())
                        {
                            sb.append(",");
                        }
                    }
                    sb.append(']');
                }
                else if (value instanceof Map)
                {
                    sb.append('{');
                    for (Iterator<Map.Entry<String, Object>> iterator = ((Map<String, Object>) value).entrySet()
                        .iterator(); iterator.hasNext(); )
                    {
                        Map.Entry<String, Object> e = iterator.next();
                        sb.append(JSONUtil.DEFAULT_GENERATOR.quote(e.getKey()));
                        sb.append(':');
                        dump(
                            sb,
                            e.getValue(),
                            visited,
                            maxDepth - 1
                        );

                        if (iterator.hasNext())
                        {
                            sb.append(",");
                        }
                    }
                    sb.append('}');

                }
                else if (value instanceof JSObject)
                {
                    JSObject jsObject = (JSObject)value;

                    if (jsObject.isArray())
                    {
                        sb.append('[');
                        final int length = (int) jsObject.getMember("length");
                        for (int i=0; i < length; i++)
                        {
                            if (i > 0)
                            {
                                sb.append(",");
                            }
                            dump(
                                sb,
                                jsObject.getSlot(i),
                                visited,
                                maxDepth - 1
                            );

                        }
                        sb.append(']');

                    }
                    else if (jsObject.isFunction())
                    {
                        sb.append(jsObject);
                    }
                    else
                    {
                        sb.append('{');
                        for (Iterator<String> iterator = jsObject.keySet().iterator(); iterator.hasNext(); )
                        {
                            String name = iterator.next();
                            sb.append(name);
                            sb.append(':');
                            dump(
                                sb,
                                jsObject.getMember(name),
                                visited,
                                maxDepth - 1
                            );

                            if (iterator.hasNext())
                            {
                                sb.append(",");
                            }
                        }
                        sb.append('}');
                    }
                }
                else
                {
                    final JSONClassInfo classInfo = JSONUtil.OBJECT_SUPPORT.createClassInfo(value.getClass());

                    sb.append('{');
                    for (Iterator<JSONPropertyInfo> iterator = classInfo.getPropertyInfos().iterator(); iterator
                        .hasNext(); )
                    {
                        JSONPropertyInfo info = iterator.next();
                        if (info.isReadable() && !info.isIgnore())
                        {
                            final String name = info.getJsonName();
                            final Object property = JSONUtil.DEFAULT_UTIL.getProperty(value, name);
                            if (property != null || !info.isIgnoreIfNull())
                            {
                                sb.append(name);
                                sb.append(':');
                                dump(
                                    sb,
                                    property,
                                    visited,
                                    maxDepth - 1
                                );

                            }
                        }
                        if (iterator.hasNext())
                        {
                            sb.append(",");
                        }
                    }
                    sb.append('}');
                }
            }
        }
    }
}
