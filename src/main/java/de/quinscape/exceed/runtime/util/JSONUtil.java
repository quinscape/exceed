package de.quinscape.exceed.runtime.util;

import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.info.JavaObjectSupport;
import org.svenson.util.JSONBeanUtil;
import org.svenson.util.JSONBuilder;

/**
 * JSON helper utils methods.
 */
public class JSONUtil
{
    public static String formatJSON(String s)
    {
        return JSON.formatJSON(s);
    }

    private final static String OK_RESPONSE = "{\"ok\":true}";

    public final static JavaObjectSupport OBJECT_SUPPORT = new JavaObjectSupport();
    public final static JSONParser DEFAULT_PARSER;
    public final static JSONBeanUtil DEFAULT_UTIL;
    static
    {
        final JSONParser jsonParser = new JSONParser();
        jsonParser.setObjectSupport(OBJECT_SUPPORT);
        JSONBeanUtil util = new JSONBeanUtil();
        util.setObjectSupport(OBJECT_SUPPORT);
        DEFAULT_PARSER = jsonParser;
        DEFAULT_UTIL = util;
    }

    public static String ok()
    {
        return OK_RESPONSE;
    }

    public static String error(Throwable err)
    {
        return error(err.getMessage());
    }

    public static String error(String message)
    {
        return error(message, null);
    }

    public static String error(String message, Object payload)
    {
        return JSONBuilder.buildObject()
            .property("ok", false)
            .property("error", message)
            .propertyUnlessNull("detail", payload)
            .output();
    }
}
