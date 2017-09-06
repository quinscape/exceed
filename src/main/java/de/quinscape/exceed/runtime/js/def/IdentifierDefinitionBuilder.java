package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.env.PropertyTypeResolver;

import java.util.Map;

public final class IdentifierDefinitionBuilder
    extends DefinitionBuilderBase
{
    public IdentifierDefinitionBuilder(DefinitionsBuilder parent, String name)
    {
        super(parent, name);
    }

    public Definitions build()
    {
        validate();



        root.addDefinition(
            name,
            new IdentifierDefinition(
                name,
                propertyType,
                resolver,
                description,
                type,
                definitionRenderer,
                chapter != null ? chapter : root.getChapter()
            )
        );

        return root.build();
    }

    public Map<String, Definition> buildMap()
    {
        return build().getDefinitions();
    }

    public IdentifierDefinitionBuilder withPropertyType(PropertyModel target)
    {
        this.propertyType = target;
        return this;
    }

    public IdentifierDefinitionBuilder withPropertyType(String type)
    {
        this.propertyType = DomainProperty.builder().withType(type).build();
        return this;
    }


    public IdentifierDefinitionBuilder withPropertyType(String type, String typeParam)
    {
        this.propertyType = DomainProperty.builder().withType(type, typeParam).build();
        return this;
    }


    public IdentifierDefinitionBuilder withPropertyType(String type, String typeParam, Map<String,Object> config)
    {
        this.propertyType = DomainProperty.builder().withType(type, typeParam).withConfigMap(config).build();
        return this;
    }


    public IdentifierDefinitionBuilder withPropertyTypeResolver(PropertyTypeResolver resolver)
    {
        this.resolver = resolver;
        return this;
    }

    @Override
    public IdentifierDefinitionBuilder withDescription(String description)
    {
        return (IdentifierDefinitionBuilder) super.withDescription(description);
    }

    @Override
    public IdentifierDefinitionBuilder renderAs(String replacement)
    {
        return (IdentifierDefinitionBuilder) super.renderAs(replacement);
    }

    @Override
    public IdentifierDefinitionBuilder withType(DefinitionType type)
    {
        super.withType(type);
        return this;
    }

    public IdentifierDefinitionBuilder renderWith(DefinitionRenderer definitionRenderer)
    {
        this.definitionRenderer = definitionRenderer;
        return this;
    }

    public IdentifierDefinitionBuilder chapter(String chapter)
    {
        return (IdentifierDefinitionBuilder) super.chapter(chapter);
    }
}

