package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalUnit;
import java.util.TimeZone;

public class DateConverter
    implements PropertyConverter<Date,String>
{
    @Override
    public Date convertToJava(RuntimeContext runtimeContext, String value, DomainProperty param)
    {
        Instant instant = Instant.parse(value);
        return new Date(instant.toEpochMilli());
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Date value, DomainProperty property)
    {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'00:00'Z'");
        df.setTimeZone(tz);

        return df.format(value);
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
