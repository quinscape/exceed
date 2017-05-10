package de.quinscape.exceed.model.context;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.view.AttributeValue;
import org.svenson.JSONProperty;

/**
 * A single scoped property value within a scope.
 */
public class ScopedPropertyModel
{
    private String name;

    private String description;

    private String type;

    private Object typeParam;

    private AttributeValue defaultValue;

    private int maxLength = -1;

    private boolean fromLayout;


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
    @DocumentedModelType("Expression")
    public String getDefaultValue()
    {
        return defaultValue != null ? defaultValue.getValue() : null;
    }

    @JSONProperty(ignore = true)
    public ASTExpression getDefaultValueExpression()
    {
        return defaultValue != null ? defaultValue.getAstExpression() : null;
    }


    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = AttributeValue.forValue(defaultValue, true);
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
    public Object getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
    }


    /**
     * For <code>PlainText</code> and  <code>RichText</code> type, this defines the maximum number of characters.
     * If it is less or equal to 0, the property is unbounded.
     * @return
     */
    public int getMaxLength()
    {
        return maxLength;
    }


    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }


    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    /**
     * The name of the scoped property. Must be unique for all scope locations, i.e. within the combination of all
     * applicable scopes the each context.
     */
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
    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + getName() + '\''
            + "type = '" + type + '\''
            + ", typeParam = " + typeParam
            + ", defaultValue = " + defaultValue
            + ", required = " + required
            + ", maxLength = " + maxLength
            ;
    }
}
