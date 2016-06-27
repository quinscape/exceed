package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.AttributeValue;
import org.svenson.JSONProperty;

import java.util.Map;

public class DomainProperty
{
    public final static String DATA_LIST_ROOT_PROPERTY_TYPE = "DataListRoot";

    public final static String DATA_LIST_PROPERTY_TYPE = "DataList";

    public final static String DOMAIN_TYPE_PROPERTY_TYPE = "DomainType";

    private String name;

    private String type;

    private Object typeParam;

    private AttributeValue defaultValue;

    private boolean required;

    private int maxLength;

    private String domainType;

    private ForeignKeyDefinition foreignKey;

    private Object data;


    public DomainProperty()
    {
        this(null, null, null, false);
    }


    public DomainProperty(String name, String type, String defaultValue, boolean required)
    {
        this(name, type, defaultValue, required, null, 0, null);
    }


    public DomainProperty(String name, String type, String defaultValue, boolean required, Object typeParam, int
        maxLength, String domainType)
    {
        this.name = name;
        this.type = type;
        this.typeParam = typeParam;
        setDefaultValue(defaultValue);
        this.required = required;
        this.maxLength = maxLength;
        this.domainType = domainType;
    }


    /**
     * Name of the property.
     *
     * @return
     */
    @JSONProperty(priority = 100)
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Logical property type for this property.
     *
     * @return
     */
    @JSONProperty(priority = 90)
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * Type parameter for the property. Set for List, Map, Enum.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 80)
    public Object getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
    }


    /**
     * Set true if this property is required.
     *
     * @return
     */
    @JSONProperty(priority = 70)
    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    /**
     * Maximum length for this property if applicable.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 60)
    public int getMaxLength()
    {
        return maxLength;
    }


    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }


    /**
     * Default value for this property.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 50)
    public String getDefaultValue()
    {
        return defaultValue != null ? defaultValue.getValue() : null;
    }


    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = AttributeValue.forValue(defaultValue, true);
    }


    @JSONProperty(ignore = true)
    public ASTExpression getDefaultValueExpression()
    {
        return defaultValue != null ? defaultValue.getAstExpression() : null;
    }


    /**
     * User data field. Not used by the system.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 40)
    public Object getData()
    {
        return data;
    }


    public void setData(Object data)
    {
        this.data = data;
    }


    @JSONProperty(ignoreIfNull = true)
    public String getDomainType()
    {
        return domainType;
    }


    public void setDomainType(String domainType)
    {
        this.domainType = domainType;
    }


    @JSONProperty(ignoreIfNull = true, priority = 30)
    public Object getForeignKey()
    {
        if (foreignKey == null)
        {
            return null;
        }

        if (foreignKey.getProperty().equals(DomainType.ID_PROPERTY))
        {
            return foreignKey.getType();
        }
        else
        {
            return foreignKey;
        }
    }


    @JSONProperty(ignore = true)
    public ForeignKeyDefinition getForeignKeyDefinition()
    {
        return foreignKey;
    }


    public void setForeignKey(Object foreignKey)
    {
        if (foreignKey == null)
        {
            this.foreignKey = null;
        }
        else if (foreignKey instanceof String)
        {
            final ForeignKeyDefinition def = new ForeignKeyDefinition();
            def.setType((String) foreignKey);
            this.foreignKey = def;
        }
        else if (foreignKey instanceof Map)
        {
            final ForeignKeyDefinition def = new ForeignKeyDefinition();
            final Object typeValue = ((Map) foreignKey).get("type");

            final Object property = ((Map) foreignKey).get("property");

            if (typeValue == null)
            {
                throw new IllegalStateException(this + ": Invalid foreign key: 'type' can't be null");
            }

            if (!(typeValue instanceof String))
            {
                throw new IllegalStateException(this + ": Invalid foreign key: 'type' must be string");
            }
            if (property != null && !(property instanceof String))
            {
                throw new IllegalStateException(this + ": Invalid foreign key: 'property' must be string");
            }
            def.setType((String) typeValue);
            def.setProperty((String) property);
            this.foreignKey = def;
        }
        else
        {
            throw new IllegalStateException(this + ": 'foreignKey' property must be map ({ type: <type>, property: " +
                "<property>} or string (type)");
        }
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", type = '" + type + '\''
            + ", typeParam = " + typeParam
            + ", defaultValue = " + defaultValue
            + ", required = " + required
            + ", maxLength = " + maxLength
            + ", domainType = '" + domainType + '\''
            + ", foreignKey = " + foreignKey
            + ", data = " + data
            ;
    }
}
