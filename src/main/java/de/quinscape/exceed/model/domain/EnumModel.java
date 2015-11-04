package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;

import java.util.List;

public class EnumModel
    extends TopLevelModel
{
    private List<String> values;


    public void setValues(List<String> values)
    {
        this.values = values;
    }


    public List<String> getValues()
    {
        return values;
    }
}
