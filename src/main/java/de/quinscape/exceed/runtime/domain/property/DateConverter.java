package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.Date;

public class DateConverter
    implements PropertyConverter<Date,String>
{
    DateTimeFormatter formatter = ISODateTimeFormat.date();

    @Override
    public Date convertToJava(RuntimeContext runtimeContext, String value, DomainProperty param)
    {
        LocalDate localDate = formatter.parseLocalDate(value);
        return new Date(localDate.toDate().getTime());
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Date value, DomainProperty property)
    {

        return formatter.print(LocalDate.fromDateFields(value));
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
