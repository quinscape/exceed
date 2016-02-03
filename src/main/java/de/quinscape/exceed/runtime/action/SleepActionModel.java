package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.ActionModel;

public class SleepActionModel
    extends ActionModel
{
    private int time;


    public int getTime()
    {
        return time;
    }


    public void setTime(int time)
    {
        this.time = time;
    }
}
