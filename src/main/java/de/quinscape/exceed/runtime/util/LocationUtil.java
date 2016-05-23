package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocationUtil
{
    public static String evaluateURI(String template, Map<String, Object> params)
    {
        try
        {
            List<String> parts = Util.split(template, "/");

            Set<String> paramNames = new HashSet<>(params.keySet());

            StringBuilder sb = new StringBuilder();

            for (String part : parts)
            {
                sb.append('/');
                if (part.startsWith("{") && part.startsWith("{"))
                {
                    String varName = part.substring(1, part.length() - 1);
                    sb.append(params.get(varName));
                    paramNames.remove(varName);
                }
                else
                {
                    sb.append(URLEncoder.encode(part, "UTF-8"));
                }
            }


            char sep = '?';
            for (String name : paramNames)
            {
                Object paramValue = params.get(name);

                String encodedName = URLEncoder.encode(name, "UTF-8");
                if (paramValue instanceof String)
                {
                    sb.append(sep);
                    sb.append(encodedName);
                    sb.append('=');
                    sb.append(URLEncoder.encode((String) paramValue, "UTF-8"));
                    sep = '&';
                }
                else
                {
                    String[] values = (String[]) paramValue;

                    for (String value : values)
                    {
                        sb.append(sep);
                        sb.append(encodedName);
                        sb.append('=');
                        sb.append(URLEncoder.encode(value, "UTF-8"));
                        sep = '&';
                    }
                }
            }
            return sb.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ExceedRuntimeException(e);
        }

    }
}
