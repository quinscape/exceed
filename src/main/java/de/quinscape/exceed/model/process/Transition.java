package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.action.ActionModel;
import org.svenson.JSONProperty;

import java.util.List;

public class Transition
{
    private String name;

    private String from;

    private String to;

    private List<ActionModel> actions;

    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }


    @JSONProperty(ignore = true)
    public String getFrom()
    {
        return from;
    }

    public String getTo()
    {
        return to;
    }


    public void setTo(String to)
    {
        this.to = to;
    }


    public List<ActionModel> getActions()
    {
        return actions;
    }


    public void setActions(List<ActionModel> actions)
    {
        this.actions = actions;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", '" + from + "' => '" + to + "'"
            ;
    }
}
