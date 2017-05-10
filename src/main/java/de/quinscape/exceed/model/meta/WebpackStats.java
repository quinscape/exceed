package de.quinscape.exceed.model.meta;

import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.List;

/**
 * Contains statistics about the current webpack build of the exceed client sources.
 */
public class WebpackStats
{
    private final List<WebpackEntryPoint> entries;


    public WebpackStats(
        @JSONParameter("entries")
        @JSONTypeHint(WebpackEntryPoint.class)
        List<WebpackEntryPoint> entries
    )
    {
        this.entries = entries;
    }


    /**
     * Returns the list of Javascript Entry points.
     *
     * @return list of entry points.
     */
    public List<WebpackEntryPoint> getEntries()
    {
        return entries;
    }
}
