package de.quinscape.exceed.tooling;

import org.svenson.JSONParameter;

import java.util.List;

public class ModelDoc
{
    private String type;

    private String locationDescription;

    private String classDescription;

    private List<ModelPropertyDoc> propertyDocs;


    public ModelDoc(String modelType)
    {
        this.type = modelType;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public void setLocationDescription(String locationDescription)
    {
        this.locationDescription = locationDescription;
    }


    public void setClassDescription(String classDescription)
    {
        this.classDescription = classDescription;
    }


    public void setPropertyDocs(List<ModelPropertyDoc> propertyDocs)
    {
        this.propertyDocs = propertyDocs;
    }


    public String getType()
    {
        return type;
    }


    public String getLocationDescription()
    {
        return locationDescription;
    }


    public String getClassDescription()
    {
        return classDescription;
    }


    public List<ModelPropertyDoc> getPropertyDocs()
    {
        return propertyDocs;
    }
}
