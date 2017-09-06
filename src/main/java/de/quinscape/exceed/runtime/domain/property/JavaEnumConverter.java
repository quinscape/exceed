package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

/**
 * A converter that uses java enums as fields for a domain type.
 */
public class JavaEnumConverter
    implements PropertyConverter<Enum, String, String>
{
    private final Class<? extends Enum> enumClass;


    public JavaEnumConverter(Class<? extends Enum> enumClass)
    {
        this.enumClass = enumClass;

    }

    @Override
    public Enum convertToJava(RuntimeContext runtimeContext, String value)
    {
        return Enum.valueOf(enumClass, value);
    }


    @Override
    public String convertToJSON(RuntimeContext runtimeContext, Enum value)
    {
        return value.name();
    }


    @Override
    public String convertToJs(RuntimeContext runtimeContext, Enum value)
    {
        return value.name();
    }


    @Override
    public Enum convertFromJs(RuntimeContext runtimeContext, String value)
    {
        return Enum.valueOf(enumClass, value);
    }


    @Override
    public Class<Enum> getJavaType()
    {
        return Enum.class;
    }


    @Override
    public Class<String> getJSONType()
    {
        return String.class;
    }
}
