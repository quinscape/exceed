package de.quinscape.exceed.model.domain.migration;

import de.quinscape.exceed.model.domain.MigrationStepModel;

public class RenamePropertyModel
    extends MigrationStepModel
{
    private String type;

    private String from;

    private String to;


    public RenamePropertyModel()
    {
        super("renamePropertyStep");
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getFrom()
    {
        return from;
    }


    public void setFrom(String from)
    {
        this.from = from;
    }


    public String getTo()
    {
        return to;
    }


    public void setTo(String to)
    {
        this.to = to;
    }
}
