package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

public class EnumConverter
    implements PropertyConverter<Integer, String>
{
    @Override
    public Integer convertToJava(RuntimeContext runtimeContext, String value, DomainProperty property)
    {
        Object param = property.getTypeParam();
        if (param instanceof List)
        {
            List<String> values = (List<String>) param;
            for (int i = 0; i < (values).size(); i++)
            {
                String v = values.get(i);
                if (value.equals(v))
                {
                    return i;
                }
            }
            throw new ConverterException("'" + value + "' is not one of " + param);
        }
        else
        {
            throw new ConverterException("Enum type parameter must be a list of strings");

        }
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Integer value, DomainProperty property)
    {
        Object param = property.getTypeParam();
        if (param instanceof List)
        {
            List<String> values = (List<String>) param;
            if (value < 0 || value > values.size())
            {
                throw new IndexOutOfBoundsException("No enum value for index " + value);
            }
            return values.get(value);
        }
        else
        {
            throw new ConverterException("Enum type parameter must be a list of strings");
        }
    }

    @Override
    public Class<Integer> getJavaType()
    {
        return Integer.class;
    }

    @Override
    public Class<String> getJSONType()
    {
        return String.class;
    }
}
