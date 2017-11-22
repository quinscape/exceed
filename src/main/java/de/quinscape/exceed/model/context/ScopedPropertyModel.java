package de.quinscape.exceed.model.context;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.merge.ModelMergeMode;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.svenson.JSONProperty;

import java.util.Map;

/**
 * A single scoped property value within a scope.
 */
@MergeStrategy(ModelMergeMode.REPLACE)
public class ScopedPropertyModel
    implements PropertyModel
{
    private String name;

    private String description;

    private String type;

    private String typeParam;

    private ExpressionValue defaultValue;

    private int maxLength = -1;

    private boolean fromLayout;

    private boolean required;

    private Map<String, Object> config;

    private PropertyType propertyType;

    private boolean isPrivate;

    public ScopedPropertyModel()
    {

    }

    /**
     * The property type of the scoped property.
     *
     * @see PropertyType#DOMAIN_TYPE
     * @see PropertyType#MAP
     * @see PropertyType#LIST
     *
     * @return
     */
    @Override
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * The default value expression for this scoped property.
     */
    @Override
    @DocumentedModelType("Expression")
    public String getDefaultValue()
    {
        return defaultValue != null ? defaultValue.getValue() : null;
    }

    @Override
    @JSONProperty(ignore = true)
    public ASTExpression getDefaultValueExpression()
    {
        return defaultValue != null ? defaultValue.getAstExpression() : null;
    }


    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = ExpressionValue.forValue(defaultValue, true);
    }


    /**
     * Property type specific type parameter:
     * <ul>
     *     <li>
     *          For the simple property types, this is <code>null</code>,
     *     </li>
     *     <li>
     *          For the <code>DomainType</code> type this is the name of the property type.
     *     </li>
     *     <li>
     *          For the <code>Map</code> and <code>List</code> types the type param is either
     *          a domain type name or a property type name.
     *     </li>
     * </p>
     */
    @Override
    public String getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(String typeParam)
    {
        this.typeParam = typeParam;
    }


    /**
     * For <code>PlainText</code> and  <code>RichText</code> type, this defines the maximum number of characters.
     * If it is less or equal to 0, the property is unbounded.
     * @return
     */
    @Override
    public int getMaxLength()
    {
        return maxLength;
    }


    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    /**
     * The name of the scoped property. Must be unique for all scope locations, i.e. within the combination of all
     * applicable scopes the each context.
     */
    @Override
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Description of this scoped property
     */
    @Override
    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @JSONProperty(ignore = true)
    public boolean isFromLayout()
    {
        return fromLayout;
    }


    public void setFromLayout(boolean fromLayout)
    {
        this.fromLayout = fromLayout;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + getName() + '\''
            +  ExpressionUtil.describe(this)
            + ", defaultValue = " + defaultValue
            + ", maxLength = " + maxLength
            ;
    }


    @Override
    @Internal
    @JSONProperty(ignore = true)
    public PropertyType getPropertyType()
    {
        return propertyType;
    }


    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }


    @Override
    public <T> T getConfig(String name, T defaultValue)
    {
        if (config == null)
        {
            return defaultValue;
        }

        final T value = (T) config.get(name);
        if (value == null)
        {
            return defaultValue;
        }
        return value;
    }

    public void setConfig(Map<String, Object> config)
    {
        this.config = config;
    }


    /**
     * Property type config
     */
    @JSONProperty(ignoreIfNull = true)
    public Map<String, Object> getConfig()
    {
        return config;
    }


    /**
     * Set  to <code>true</code> for a requires / not nullable property.
     * @return
     */
    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    /**
     * Returns <code>true</code> if the scope property is supposed to be private, that is it is only visible inside
     * the process. It is not accepting input from the outside / a parent process nor is it accessible as
     * sub process output.
     *
     * @return
     */
    public boolean isPrivate()
    {
        return isPrivate;
    }


    public void setPrivate(boolean aPrivate)
    {
        isPrivate = aPrivate;
    }
}
