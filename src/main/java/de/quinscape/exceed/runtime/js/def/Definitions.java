package de.quinscape.exceed.runtime.js.def;

import java.util.HashMap;
import java.util.Map;

public class Definitions
{
    private final Map<String,Definition> definitions;


    public Definitions()
    {
        definitions = new HashMap<>();
    }

    public void addDefinition(String name, Definition definition)
    {
        definitions.put(name, definition);
    }


    public Map<String, Definition> getDefinitions()
    {
        return definitions;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + definitions;
    }


    public Definition getDefinition(String name)
    {
        return definitions.get(name);
    }
}
