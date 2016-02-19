package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;
import java.util.Map;

public class EnumConverter
    implements PropertyConverter<Integer, String>
{
    @Override
    public Integer convertToJava(RuntimeContext runtimeContext, String value, DomainProperty property)
    {
        Map<String, EnumModel> enums = runtimeContext.getRuntimeApplication().getDomainService().getEnums();

        Object param = property.getTypeParam();
        if (param instanceof String)
        {
            String name = (String)param;
            EnumModel enumModel = enums.get(name);
            if (enumModel != null)
            {
                return enumModel.findOrdinal(value);
            }
        }
        throw new ConverterException("Invalid enum type: " + param);
    }

    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Integer value, DomainProperty property)
    {
        Map<String, EnumModel> enums = runtimeContext.getRuntimeApplication().getDomainService().getEnums();

        Object param = property.getTypeParam();
        if (param instanceof String)
        {
            String name = (String)param;
            EnumModel enumModel = enums.get(name);
            if (enumModel != null)
            {
                return enumModel.forOrdinal(value);
            }
        }
        throw new ConverterException("Invalid enum type: " + param);
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
