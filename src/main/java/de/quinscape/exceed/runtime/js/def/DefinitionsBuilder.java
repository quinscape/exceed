package de.quinscape.exceed.runtime.js.def;

import java.util.HashMap;
import java.util.Map;

public class DefinitionsBuilder
{
    private Definitions definitions = new Definitions();

    private Map<String, DefinitionBuilderBase> openBuilders = new HashMap<>();

    private String chapter = "misc";

    public DefinitionsBuilder()
    {
    }


    public Definitions build()
    {
        closeOpen();

        return definitions;
    }


    private void closeOpen()
    {
        for (DefinitionBuilderBase builder : openBuilders.values())
        {
            builder.build();
        }
    }


    public DefinitionsBuilder chapter(String chapter)
    {
        closeOpen();
        this.chapter = chapter;
        return this;
    }


    String getChapter()
    {
        return chapter;
    }


    public FunctionDefinitionBuilder function(String name)
    {
        final FunctionDefinitionBuilder builder = new FunctionDefinitionBuilder(this, name);

        openBuilders.put(name, builder);

        return builder;
    }

    public IdentifierDefinitionBuilder identifier(String name)
    {
        final IdentifierDefinitionBuilder builder = new IdentifierDefinitionBuilder(this, name);
        openBuilders.put(name, builder);
        return builder;
    }

    void addDefinition(String name, Definition definition)
    {
        openBuilders.remove(name);

        final Definition existing = definitions.getDefinition(name);
        if (existing != null)
        {
            throw new InvalidFunctionException("Duplicate definition for '" + name + "': " + existing + ", " + definition);
        }

        definitions.addDefinition(name, definition);
    }

    public DefinitionsBuilder merge(Definitions functionDefinitions)
    {
        if (functionDefinitions != null)
        {
            for (Map.Entry<String, Definition> entry : functionDefinitions.getDefinitions().entrySet())
            {
                final String name = entry.getKey();
                final Definition value = entry.getValue();
                addDefinition(name, value);
            }
        }

        return this;
    }
}
