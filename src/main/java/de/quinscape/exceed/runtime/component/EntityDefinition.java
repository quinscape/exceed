package de.quinscape.exceed.runtime.component;

import org.svenson.JSONProperty;

import java.util.List;

public class EntityDefinition
{
    private final String type;

    private final List<String> pkFields;


    public EntityDefinition(String type, List<String> pkFields)
    {
        this.type = type;
        this.pkFields = pkFields;
    }

    @JSONProperty(priority = 100)
    public String getType()
    {
        return type;
    }


    public List<String> getPkFields()
    {
        return pkFields;
    }
}

