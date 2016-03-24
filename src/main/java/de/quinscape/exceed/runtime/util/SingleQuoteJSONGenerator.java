package de.quinscape.exceed.runtime.util;

import org.svenson.JSON;

/**
 * JSON generator using single quotes quoting. Util class to reuse one instance of that in this project.
 */
public class SingleQuoteJSONGenerator
    extends JSON
{
    public final static SingleQuoteJSONGenerator INSTANCE = new SingleQuoteJSONGenerator();

    private SingleQuoteJSONGenerator()
    {
        super('\'');
    }
}
