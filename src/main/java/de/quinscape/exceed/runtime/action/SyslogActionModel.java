package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.ActionModel;

import java.util.List;

public class SyslogActionModel
    extends ActionModel
{
    public List getArgs()
    {
        return args;
    }


    public void setArgs(List args)
    {
        this.args = args;
    }


    private List args;
}
