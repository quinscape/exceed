package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.domain.GeneratedDomainObject;

import java.sql.Date;

public class Bar
    extends GeneratedDomainObject
{
    private String value;
    private Date bar;


    public String getValue()
    {
        return value;
    }


    public void setValue(String value)
    {
        this.value = value;
    }


    public Date getBar()
    {
        return bar;
    }


    public void setBar(Date bar)
    {
        this.bar = bar;
    }
}
