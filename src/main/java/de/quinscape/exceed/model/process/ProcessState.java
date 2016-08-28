package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.Model;
import org.svenson.JSONProperty;

public abstract class ProcessState
    extends Model
{
    private String name;

    private Process process;


    public String getName()
    {
        return name;
    }


    @JSONProperty(ignore = true)
    public void setName(String name)
    {
        this.name = name;
    }


    public void postProcess()
    {
        // empty by default
    }


    @JSONProperty(ignore = true)
    public Process getProcess()
    {
        return process;
    }


    public void setProcess(Process process)
    {
        this.process = process;
    }
}
