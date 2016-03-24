package de.quinscape.exceed.runtime.util;

import org.svenson.JSON;
import org.svenson.JSONParser;

/**
 * JSON generator using single quotes quoting. Util class to reuse one instance of that in this project.
 */
public class SingleQuoteJSONParser
    extends JSONParser
{
    public final static SingleQuoteJSONParser INSTANCE = new SingleQuoteJSONParser();

    private SingleQuoteJSONParser()
    {
        setAllowSingleQuotes(true);
    }
}
