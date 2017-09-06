package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.js.env.PropertyTypeResolver;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class IdentifierDefinition
    implements Definition
{
    private final String name;

    private final PropertyModel propertyType;

    private final PropertyTypeResolver resolver;

    private final String chapter;

    private String description;

    private final DefinitionType type;

    private final DefinitionRenderer definitionRenderer;


    public IdentifierDefinition(
        String name,
        PropertyModel propertyType,
        PropertyTypeResolver resolver,
        String description,
        DefinitionType type,
        DefinitionRenderer definitionRenderer,
        String chapter
    )
    {
        this.chapter = chapter;
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (propertyType == null && resolver == null)
        {
            throw new IllegalArgumentException("propertyType and resolver can't both be null");
        }

        if (propertyType != null && resolver != null)
        {
            throw new IllegalArgumentException("propertyType and resolver can't both be defined. Define either a property type or a resolver");
        }

        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        this.name = name;
        this.propertyType = propertyType;
        this.resolver = resolver;
        this.description = description;
        this.type = type;
        this.definitionRenderer = definitionRenderer;
    }

    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public DefinitionType getDefinitionType()
    {
        return type;
    }


    public PropertyModel getPropertyType()
    {
        return propertyType;
    }


    public PropertyTypeResolver getResolver()
    {
        return resolver;
    }


    @Override
    public PropertyModel getType(TypeAnalyzerContext context, ResolvableNode astIdentifier, ExpressionModelContext contextModel)
    {
        if (contextModel == null)
        {
            throw new IllegalArgumentException("contextModel can't be null");
        }


        if (resolver != null)
        {
            return resolver.resolve(context, astIdentifier, contextModel);
        }
        else
        {
            return propertyType;
        }
    }


    @Override
    public boolean isFunction()
    {
        return false;
    }


    @Override
    public DefinitionRenderer getDefinitionRenderer()
    {
        return definitionRenderer;
    }


    @Override
    public String toString()
    {
        return name + ": " + (propertyType != null ? ExpressionUtil.describe(propertyType) : resolver ) + "( type = " + type + ")";
    }


    @Override
    public int compareTo(Definition o)
    {
        if (o instanceof FunctionDefinition)
        {
            return -1;
        }

        return getName().compareTo(o.getName());
    }


    public DefinitionType getType()
    {
        return type;
    }


    public String getChapter()
    {
        return chapter;
    }
    
}
