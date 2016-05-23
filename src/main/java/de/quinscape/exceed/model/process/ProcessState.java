package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.Model;
import org.svenson.JSONProperty;

public abstract class ProcessState
    extends Model
{
    private String name;


    public String getName()
    {
        return name;
    }


    @JSONProperty(ignore = true)
    public void setName(String name)
    {
        this.name = name;
    }


    public void validate(Process process)
    {
        // empty by default
    }
}
