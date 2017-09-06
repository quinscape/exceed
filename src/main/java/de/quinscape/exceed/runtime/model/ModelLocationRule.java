package de.quinscape.exceed.runtime.model;

import org.svenson.JSONProperty;

public class ModelLocationRule
{
    private final String prefix;

    private final String suffix;

    private final String type;


    public ModelLocationRule(String prefix, String type)
    {
        final int pos = prefix.indexOf('*');
        if (pos >= 0)
        {
            this.prefix = prefix.substring(0, pos);
            this.suffix = prefix.substring(pos + 1);
        }
        else
        {
            this.prefix = prefix;
            this.suffix = null;
        }
        this.type = type;
    }


    @JSONProperty(priority = 20)
    public String getPrefix()
    {
        return prefix;
    }


    public String getType()
    {
        return type;
    }


    public boolean matches(String path)
    {
        return path.startsWith(prefix) && (suffix == null || path.indexOf(suffix) >= prefix.length());
    }


    @JSONProperty(priority = 10, ignoreIfNull = true)
    public String getSuffix()
    {
        return suffix;
    }
}
