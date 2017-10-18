package de.quinscape.exceed.model.domain.property;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONProperty;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public final class DomainProperty
    implements PropertyModel
{
    private String name;

    private String type;

    private String typeParam;

    private ExpressionValue defaultValue;

    private boolean required;

    private int maxLength;

    private String domainType;

    private ForeignKeyDefinition foreignKey;

    private Object data;

    private String description;

    private Map<String, Object> config;

    private PropertyType propertyType;


    public DomainProperty()
    {
        this(null, null, null, false);
    }


    public DomainProperty(String name, String type, String defaultValue, boolean required)
    {
        this(name, type, defaultValue, required, null, 0, null);
    }


    public DomainProperty(String name, String type, String defaultValue, boolean required, String typeParam, int
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


    @Override
    public String getDescription()
    {
        return description;
    }


    @JSONProperty(ignoreIfNull = true)
    public Map<String, Object> getConfig()
    {
        return config;
    }


    public void setConfig(Map<String, Object> config)
    {
        this.config = config;
    }


    public void setDescription(String description)
    {
        this.description = description;
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
    public String getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(String typeParam)
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
        this.defaultValue = ExpressionValue.forValue(defaultValue, true);
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


    /**
     * Name of the domain type this property belongs to. Will be filled by the system automatically
     * @return
     */
    @Internal
    @JSONProperty(ignoreIfNull = true)
    public String getDomainType()
    {
        return domainType;
    }


    public void setDomainType(String domainType)
    {
        this.domainType = domainType;
    }


    /**
     * Foreign key definition for this property.
     */
    @JSONProperty(ignoreIfNull = true, priority = 30)
    public ForeignKeyDefinition getForeignKey()
    {
        return foreignKey;
    }


    public void setForeignKey(ForeignKeyDefinition foreignKey)
    {
        this.foreignKey = foreignKey;
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

    @JSONProperty(ignore = true)
    public String getTranslationTag()
    {
        return domainType + ":" + name;
    }

    public static DomainPropertyBuilder builder()
    {
        return new DomainPropertyBuilder();
    }


    @JSONProperty(ignore = true)
    @Override
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


    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof DomainProperty)
        {
            DomainProperty that = (DomainProperty)obj;

            return this.type.equals(that.type) &&
                Objects.equals(this.typeParam, that.typeParam) &&
                Objects.equals(this.config, that.config);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Util.hashcodeOver(type, typeParam, config);
    }


    public void postProcess(ApplicationModel applicationModel)
    {
        PropertyType.get(applicationModel, this);

        final String propertyName = getName();
        if (DomainType.ID_PROPERTY.equals(propertyName))
        {
            setRequired(true);
        }

        if (PropertyType.UUID.equals(getType()) && getMaxLength() <= 0)
        {
            setMaxLength(36);
        }

        if (PropertyType.STATE.equals(getType()))
        {
            setRequired(true);

            if (getDefaultValue() == null)
            {
                setDefaultValue(getTypeParam() + "." + StateMachine.START);
            }

            if (getMaxLength() <= 0)
            {
                final StateMachine stateMachine = applicationModel.getStateMachines().get(getTypeParam());

                final int maxLengthOfStates =
                    stateMachine.getStates()
                        .keySet()
                        .stream().max(Comparator.comparingInt(String::length))
                    .orElse("").length();

                setMaxLength(maxLengthOfStates);
            }
        }
    }
}


