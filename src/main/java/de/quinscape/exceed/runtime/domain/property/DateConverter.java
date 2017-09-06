package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import jdk.nashorn.api.scripting.JSObject;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateConverter
    implements PropertyConverter<Date,String,JSObject>
{
    private final static long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    @Override
    public Date convertToJava(RuntimeContext runtimeContext, String value)
    {
        if (value == null)
        {
            return null;
        }

        LocalDate data = LocalDate.parse(value);
        return new Date(data.toEpochDay() * MILLIS_PER_DAY);
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Date value)
    {
        if (value == null)
        {
            return null;
        }

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(tz);

        return df.format(value);
    }


    @Override
    public JSObject convertToJs(RuntimeContext runtimeContext, Date value)
    {
        return (JSObject) runtimeContext.getJsEnvironment().convertViaJSONToJs(runtimeContext, value, ExpressionUtil.DATE_TYPE);
    }


    @Override
    public Date convertFromJs(RuntimeContext runtimeContext, JSObject value)
    {
        return (Date) runtimeContext.getJsEnvironment().convertViaJSONFromJs(runtimeContext, value, ExpressionUtil.DATE_TYPE);
    }


    @Override
    public Class<Date> getJavaType()
    {
        return Date.class;
    }

    @Override
    public Class<String> getJSONType()
    {
        return String.class;
    }
}
