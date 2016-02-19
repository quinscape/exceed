package de.quinscape.exceed.runtime.editor.completion;

public class PropSuggestion
{
    private final String value;

    private final String meta ;
    private final String description;

    private final boolean wizard;


    public PropSuggestion(String value, String meta)
    {
        this(value, meta, null, false);
    }


    public PropSuggestion(String value, String meta, String description, boolean wizard)
    {
        this.value = value;
        this.meta = meta;
        this.description = description;
        this.wizard = wizard;
    }


    public String getValue()
    {
        return value;
    }


    public String getDescription()
    {
        return description;
    }


    public String getMeta()
    {
        return meta;
    }


    public boolean isWizard()
    {
        return wizard;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "value = '" + value + '\''
            + ", description = '" + description + '\''
            ;
    }
}
