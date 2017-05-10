package de.quinscape.exceed.runtime.service.search;

/**
 * Detail information for a SCOPE match
 */
public class ScopeResultDetail
{
    private final String name;

    private final String contextType;


    public ScopeResultDetail(String name, String contextType)
    {
        this.name = name;
        this.contextType = contextType;
    }


    public String getName()
    {
        return name;
    }


    public String getContextType()
    {
        return contextType;
    }
}
