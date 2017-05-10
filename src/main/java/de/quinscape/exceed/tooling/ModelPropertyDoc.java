package de.quinscape.exceed.tooling;

import java.util.Collections;
import java.util.List;

public class ModelPropertyDoc
{
    private final String name;

    private final String typeDescription;

    private final String propertyDescription;

    private final List<String> subTypeDocs;


    public ModelPropertyDoc(String name, String typeDescription, String propertyDescription, List<String> subTypeDocs)
    {
        this.name = name;
        this.typeDescription = typeDescription;
        this.propertyDescription = propertyDescription;
        this.subTypeDocs = subTypeDocs != null ? subTypeDocs : Collections.emptyList();
    }


    public String getName()
    {
        return name;
    }


    public String getTypeDescription()
    {
        return typeDescription;
    }


    public List<String> getSubTypeDocs()
    {
        return subTypeDocs;
    }


    public String getPropertyDescription()
    {
        return propertyDescription;
    }
}
