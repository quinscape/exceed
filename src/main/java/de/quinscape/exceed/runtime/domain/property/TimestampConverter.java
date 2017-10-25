package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import jdk.nashorn.api.scripting.JSObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

public class TimestampConverter
    implements PropertyConverter<Timestamp, String, JSObject>
{
    @Override
    public Timestamp convertToJava(RuntimeContext runtimeContext, String value)
    {
        if (value == null)
        {
            return null;
        }

        Instant instant = Instant.parse(value);
        return new Timestamp(instant.toEpochMilli());
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Timestamp value)
    {

        if (value == null)
        {
            return null;
        }
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        return df.format(value);
    }

    @Override
    public JSObject convertToJs(RuntimeContext runtimeContext, Timestamp value)
    {
        return (JSObject) runtimeContext.getJsEnvironment().convertViaJSONToJs(runtimeContext, value, ExpressionUtil.TIMESTAMP_TYPE);
    }


    @Override
    public Timestamp convertFromJs(RuntimeContext runtimeContext, JSObject value)
    {
        return (Timestamp) runtimeContext.getJsEnvironment().convertViaJSONFromJs(runtimeContext, value, ExpressionUtil.TIMESTAMP_TYPE);
    }

    @Override
    public Class<Timestamp> getJavaType()
    {
        return Timestamp.class;
    }

    @Override
    public Class<String> getJSONType()
    {
        return String.class;
    }

}
