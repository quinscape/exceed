package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

public class TimestampConverter
    implements PropertyConverter<Timestamp, String>
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

        if (value != null)
        {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(tz);

            return df.format(value);
        }

        ;

        return null;
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
