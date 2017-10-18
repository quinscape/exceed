package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.AbstractModel;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.context.ScopeLocationModel;
import org.svenson.JSONProperty;

public abstract class ProcessState
    extends AbstractModel
    implements ScopeLocationModel
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


    @Override
    @Internal
    public String getScopeLocation()
    {
        return process.getProcessStateName(name);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            ;
    }
}
