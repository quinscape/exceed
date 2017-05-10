package de.quinscape.exceed.runtime.service.model;

import de.quinscape.exceed.model.domain.DomainProperty;

import java.util.Map;

public class ModelDataMapping
{
    private final Map<String, DomainProperty> columns;


    public ModelDataMapping(Map<String, DomainProperty> columns)
    {

        this.columns = columns;
    }


    public Map<String, DomainProperty> getColumns()
    {
        return columns;
    }
}
