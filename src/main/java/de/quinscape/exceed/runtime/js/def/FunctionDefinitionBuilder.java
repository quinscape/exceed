package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.env.PropertyTypeResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class FunctionDefinitionBuilder
    extends DefinitionBuilderBase
{
    private List<DomainProperty> parameterModels;

    private boolean isVarArgs;

    private Class<?> context;


    public FunctionDefinitionBuilder(DefinitionsBuilder parent, String name)
    {
        super(parent, name);
    }

    public Definitions build()
    {
        validate();


        final FunctionDefinition definition;

        if (resolver != null)
        {
            definition = new FunctionDefinition(
                name,
                resolver,
                description,
                restrictedTo,
                parameterModels,
                definitionRenderer,
                isVarArgs,
                type,
                chapter != null ? chapter : root.getChapter(),
                context
            );
        }
        else
        {
            definition = new FunctionDefinition(
                name,
                propertyType,
                description,
                restrictedTo,
                parameterModels,
                definitionRenderer,
                isVarArgs,
                type,
                chapter != null ? chapter : root.getChapter(),
                context
            );
        }

        root.addDefinition(name, definition);

        return root.build();
    }


    public FunctionDefinitionBuilder withReturnType(String type)
    {
        this.propertyType = DomainProperty.builder().withType(type).build();
        return this;
    }


    public FunctionDefinitionBuilder withReturnType(String type, String typeParam)
    {
        this.propertyType = DomainProperty.builder().withType(type, typeParam).build();
        return this;
    }


    public FunctionDefinitionBuilder withReturnType(String type, String typeParam, Map<String,Object> config)
    {
        this.propertyType = DomainProperty.builder().withType(type, typeParam).withConfigMap(config).build();
        return this;
    }


    public FunctionDefinitionBuilder withReturnTypeResolver(PropertyTypeResolver resolver)
    {
        this.resolver = resolver;
        return this;
    }

    public FunctionDefinitionBuilder restrictTo(ExpressionType type)
    {
        this.restrictedTo = type;
        return this;
    }


    public FunctionDefinitionBuilder withReturnType(PropertyModel target)
    {
        this.propertyType = target;
        return this;
    }


    public FunctionDefinitionBuilder renderWith(DefinitionRenderer definitionRenderer)
    {
        this.definitionRenderer = definitionRenderer;
        return this;
    }


    @Override
    public FunctionDefinitionBuilder renderAs(String replacement)
    {

        if (replacement == null)
        {
            throw new IllegalArgumentException("replacement can't be null");
        }


        return (FunctionDefinitionBuilder) super.renderAs(replacement);
    }


    public FunctionDefinitionBuilder withParameterModels(DomainProperty... parameterModels)
    {
        if (parameterModels == null || parameterModels.length == 0)
        {
            return this;
        }

        return withParameterModels(Arrays.asList(parameterModels), false);
    }


    public FunctionDefinitionBuilder withParameterModels(List<DomainProperty> parameterModels)
    {
        if (parameterModels == null || parameterModels.size() == 0)
        {
            return this;
        }

        return withParameterModels(parameterModels, false);
    }


    public FunctionDefinitionBuilder withParameterModels(List<DomainProperty> parameterModels, boolean isVarArgs)
    {
        this.parameterModels = parameterModels;
        this.isVarArgs = isVarArgs;
        return this;

    }


    public boolean isVarArgs()
    {
        return isVarArgs;
    }


    @Override
    public FunctionDefinitionBuilder withDescription(String description)
    {
        return (FunctionDefinitionBuilder) super.withDescription(description);
    }


    @Override
    public FunctionDefinitionBuilder withType(DefinitionType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        super.withType(type);
        return this;
    }


    public FunctionDefinitionBuilder chapter(String chapter)
    {
        return (FunctionDefinitionBuilder) super.chapter(chapter);
    }

    public String getChapter()
    {
        return chapter;
    }


    public FunctionDefinitionBuilder asOperationFor(Class<?> cls)
    {
        this.context = cls;
        return this;
    }
}
