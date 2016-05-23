package de.quinscape.exceed.model.action;

import de.quinscape.exceed.runtime.scope.ScopedValueType;

public class SetActionModel
    extends ActionModel
{
    private ScopedValueType type;
    private String name;
    private Object value;


    public ScopedValueType getType()
    {
        return type;
    }


    public void setType(ScopedValueType type)
    {
        this.type = type;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public Object getValue()
    {
        return value;
    }


    public void setValue(Object value)
    {
        this.value = value;
    }
}
