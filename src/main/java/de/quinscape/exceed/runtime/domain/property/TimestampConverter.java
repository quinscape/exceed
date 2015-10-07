package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.Timestamp;

public class TimestampConverter
    implements PropertyConverter<Timestamp, String>
{
    DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    @Override
    public Timestamp convertToJava(RuntimeContext runtimeContext, String value, DomainProperty param)
    {
        LocalDate localDate = formatter.parseLocalDate(value);
        return new Timestamp(localDate.toDate().getTime());
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Timestamp value, DomainProperty property)
    {

        return formatter.print(LocalDate.fromDateFields(value));
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
