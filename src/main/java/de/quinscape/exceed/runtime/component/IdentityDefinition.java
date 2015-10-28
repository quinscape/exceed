package de.quinscape.exceed.runtime.component;

import org.svenson.JSONProperty;

import java.util.List;

public class IdentityDefinition
{
    private final String type;

    private final List<String> pkFields;

    private final EntityType entityType;

    public IdentityDefinition(String type, List<String> pkFields)
    {
        this(type, pkFields, EntityType.ENTITY);
    }

    public IdentityDefinition(String type, List<String> pkFields, EntityType entityType)
    {
        this.type = type;
        this.pkFields = pkFields;
        this.entityType = entityType;
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


    public EntityType getEntityType()
    {
        return entityType;
    }
}

