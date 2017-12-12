package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.merge.ModelMergeMode;

import java.util.List;

@MergeStrategy(ModelMergeMode.DEEP)
public class BeanWithMergedList
{
    private List<String> values;


    public List<String> getValues()
    {
        return values;
    }


    public void setValues(List<String> values)
    {
        this.values = values;
    }
}
