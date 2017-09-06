package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.js.env.PropertyTypeResolver;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A formal definition for function expressions within the exceed expression language.
 *
 * @see Definition#builder() 
 */
public class FunctionDefinition
    implements Definition
{
    private final String name;

    private final PropertyModel returnType;

    private final PropertyTypeResolver returnTypeResolver;

    private final String description;

    /**
     * If <code>true</code>, the last property model in {@link #parameterModels} is a varargs parameter
     */
    private final boolean isVarArgs;

    private final DefinitionType type;

    private final ExpressionType restrictedTo;

    private final List<DomainProperty> parameterModels;

    private final DefinitionRenderer functionRenderer;

    private final String chapter;

    private final boolean embedsForeignExpression;


    public FunctionDefinition(
        String name,
        PropertyModel propertyType,
        String description,
        ExpressionType restrictedTo,
        List<DomainProperty> parameterModels,
        DefinitionRenderer functionRenderer,
        boolean isVarArgs,
        DefinitionType type,
        String chapter
    )
    {
        this(name, propertyType, null, description, restrictedTo, parameterModels, functionRenderer, isVarArgs, type, chapter);

        if (propertyType == null)
        {
            throw new IllegalArgumentException("propertyType can't be null");
        }
    }

    public FunctionDefinition(
        String name,
        PropertyTypeResolver returnTypeResolver,
        String description,
        ExpressionType restrictedTo,
        List<DomainProperty> parameterModels,
        DefinitionRenderer functionRenderer,
        boolean isVarArgs,
        DefinitionType type,
        String chapter
    )
    {
        this(name, null, returnTypeResolver, description, restrictedTo, parameterModels, functionRenderer, isVarArgs, type, chapter);

        if (returnTypeResolver == null)
        {
            throw new IllegalArgumentException("returnTypeResolver can't be null");
        }
    }


    private FunctionDefinition(
        String name,
        PropertyModel returnType,
        PropertyTypeResolver returnTypeResolver,
        String description,
        ExpressionType restrictedTo,
        List<DomainProperty> parameterModels,
        DefinitionRenderer functionRenderer,
        boolean isVarArgs,
        DefinitionType type,
        String chapter
    )
    {
        if (chapter == null)
        {
            throw new IllegalArgumentException("chapter can't be null");
        }

        this.name = name;
        this.returnType = returnType;
        this.returnTypeResolver = returnTypeResolver;
        this.description = description;
        this.restrictedTo = restrictedTo;
        this.parameterModels = parameterModels != null ? parameterModels : Collections.emptyList();
        this.functionRenderer = functionRenderer;
        this.isVarArgs = isVarArgs;
        this.type = type;
        this.chapter = chapter;

        embedsForeignExpression = this.parameterModels.indexOf(ExpressionUtil.EXPRESSION_TYPE) >= 0;
    }


    @Override
    public DefinitionRenderer getDefinitionRenderer()
    {
        return functionRenderer;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public PropertyModel getType(TypeAnalyzerContext context, ResolvableNode astFunction, ExpressionModelContext contextModel)
    {
        if (contextModel == null)
        {
            throw new IllegalArgumentException("contextModel can't be null");
        }


        if (returnTypeResolver != null)
        {
            return returnTypeResolver.resolve(context, astFunction, contextModel);
        }
        else
        {
            return returnType;
        }
    }


    public List<DomainProperty> getParameterModels()
    {
        return parameterModels;
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


    public PropertyModel getReturnType()
    {
        return returnType;
    }


    public PropertyTypeResolver getReturnTypeResolver()
    {
        return returnTypeResolver;
    }


    public ExpressionType getRestrictedTo()
    {
        return restrictedTo;
    }

    public boolean isValidIn(ExpressionType type)
    {
        return restrictedTo == null || restrictedTo == type;
    }

    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public String toString()
    {
        return name + "("+ paramList()  + ") : " + (returnType != null ? ExpressionUtil.describe(returnType) : returnTypeResolver ) + "( type = " + type + ")";
    }


    private String paramList()
    {
        if (parameterModels == null || parameterModels.size() == 0)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Iterator<DomainProperty> iterator = parameterModels.iterator(); iterator.hasNext(); )
        {
            sb.append(ExpressionUtil.describe(iterator.next()));

            if (iterator.hasNext())
            {
                sb.append(", ");
            }
        }

        return sb.toString();
    }


    public boolean isVarArgs()
    {
        return isVarArgs;
    }


    @Override
    public int compareTo(Definition o)
    {
        if (o instanceof IdentifierDefinition)
        {
            return 1;
        }

        return getName().compareTo(o.getName());
    }


    public DomainProperty getParameterModel(int i)
    {
        if (i < parameterModels.size())
        {
            return parameterModels.get(i);
        }
        else
        {
            if (isVarArgs)
            {
                return parameterModels.get(parameterModels.size() - 1);
            }
        }

        throw new InvalidExpressionException("Definition [" + this + "] has no parameter #" + i);
    }


    public boolean embedsForeignExpression()
    {
        return embedsForeignExpression;
    }


    @Override
    public String getChapter()
    {
        return chapter;
    }
}
