package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationUtil
{
    final static Pattern VAR_PATTERN = Pattern.compile("^\\{([0-9a-z_]+)(\\?)?\\}$", Pattern.CASE_INSENSITIVE);

    public final static int VAR_NAME_GROUP = 1;
    public final static int VAR_OPTIONAL_GROUP = 2;

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

                Matcher m = match(part);
                if (m.matches())
                {
                    String varName = m.group(VAR_NAME_GROUP);
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


    public static Matcher match(String name)
    {
        return VAR_PATTERN.matcher(name);
    }
}
