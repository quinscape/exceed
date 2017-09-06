package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.env.FunctionReplacementRenderer;
import de.quinscape.exceed.runtime.js.env.PropertyTypeResolver;

/**
 * Abstract base class for {@link FunctionDefinitionBuilder} and {@link IdentifierDefinitionBuilder}
 */
abstract class DefinitionBuilderBase
{
    protected final DefinitionsBuilder root;

    protected final String name;

    protected PropertyModel propertyType;

    protected PropertyTypeResolver resolver;

    protected String description;

    protected ExpressionType restrictedTo = null;

    protected DefinitionRenderer definitionRenderer;

    protected DefinitionType type;

    protected String chapter;

    DefinitionBuilderBase(DefinitionsBuilder root, String name)
    {

        if (root == null)
        {
            throw new IllegalArgumentException("root can't be null");
        }

        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        this.root = root;
        this.name = name;
    }



    protected DefinitionBuilderBase withDescription(String description)
    {
        this.description = description;
        return this;
    }

    protected DefinitionBuilderBase withType(DefinitionType type)
    {
        this.type = type;
        return this;
    }

    public FunctionDefinitionBuilder andFunction(String name)
    {
        build();
        return root.function(name);
    }

    public IdentifierDefinitionBuilder andIdentifier(String name)
    {
        build();
        return root.identifier(name);
    }

    protected void validate()
    {
        if (propertyType == null && resolver == null)
        {
            throw new IllegalStateException("You must define either a type or a resolver.");
        }

        if (propertyType != null && resolver != null)
        {
            throw new IllegalStateException("You must define either a type or a resolver, not both.");
        }
    }


    public abstract Definitions build();


    protected DefinitionBuilderBase renderAs(String replacement)
    {
        this.definitionRenderer = new FunctionReplacementRenderer(replacement);
        return this;
    }

    protected DefinitionBuilderBase chapter(String chapter)
    {
        this.chapter = chapter;
        return this;
    }


    public String getChapter()
    {
        return chapter;
    }
}
