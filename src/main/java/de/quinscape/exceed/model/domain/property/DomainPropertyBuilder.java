package de.quinscape.exceed.model.domain.property;

import de.quinscape.exceed.model.annotation.ExceedPropertyType;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import jdk.nashorn.api.scripting.JSObject;

import java.util.HashMap;
import java.util.Map;

public class DomainPropertyBuilder
{
    private String name = null;

    private String type = null;

    private String defaultValue = null;

    private boolean required = false;

    private String typeParam = null;

    private int maxLength = 0;

    private String domainType = null;

    private Map<String,Object> config = null;

    private ForeignKeyDefinition key;

    private String description;


    public DomainPropertyBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public DomainPropertyBuilder fromAnnotation(ExceedPropertyType typeAnno)
    {
        final String typeParam = typeAnno.typeParam();
        final String type = typeAnno.type();

        this.withType(type, typeParam)
            .withMaxLength(typeAnno.maxLength())
            .setRequired(typeAnno.required());

        // delete preexisting config
        if (config != null)
        {
            config = new HashMap<>();
        }

        for (ExceedPropertyType.Config config : typeAnno.config())
        {
            this.withConfig(
                config.name(),
                ExpressionUtil.evaluateSimple(
                    config.value()
                )
            );
        }
        return this;
    }


    public DomainPropertyBuilder withType(String type)
    {
        this.type = type;
        return this;
    }

    public DomainPropertyBuilder withType(String type, String typeParam)
    {
        this.type = type;
        this.typeParam = typeParam;
        return this;
    }


    public DomainPropertyBuilder withDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
        return this;
    }


    public DomainPropertyBuilder setRequired(boolean required)
    {
        this.required = required;
        return this;
    }


    public DomainPropertyBuilder withMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
        return this;
    }


    public DomainPropertyBuilder withDomainType(String domainType)
    {
        this.domainType = domainType;
        return this;
    }

    public DomainPropertyBuilder withConfig(String name, Object value)
    {
        if (config == null)
        {
            config = new HashMap<>();
        }
        config.put(name, value);
        return this;
    }

    public DomainProperty build()
    {
        final DomainProperty property = new DomainProperty(
            name,
            type,
            defaultValue,
            required,
            typeParam,
            maxLength,
            domainType
        );
        property.setConfig(config);
        property.setForeignKey(key);
        property.setDescription(description);
        return property;
    }

    public ScopedPropertyModel buildScoped()
    {
        final ScopedPropertyModel property = new ScopedPropertyModel();

        property.setName(name);
        property.setType(type);
        property.setTypeParam(typeParam);
        property.setDefaultValue(defaultValue);
        property.setConfig(config);
        property.setMaxLength(maxLength);
        property.setDescription(description);

        return property;
    }


    public DomainPropertyBuilder withConfigMap(Map<String, Object> config)
    {
        this.config = config;
        return this;
    }


    public DomainPropertyBuilder withForeignKey(String type)
    {
        return withForeignKey(type, DomainType.ID_PROPERTY);
    }

    public DomainPropertyBuilder withForeignKey(String type, String property)
    {
        key  = new ForeignKeyDefinition();
        key.setType(type);
        key.setProperty(property);

        return this;
    }


    public DomainPropertyBuilder fromProperty(DomainProperty domainProperty)
    {
        name = domainProperty.getName();
        type = domainProperty.getType();
        defaultValue = domainProperty.getDefaultValue();
        required = domainProperty.isRequired();
        typeParam = domainProperty.getTypeParam();
        maxLength = domainProperty.getMaxLength();
        domainType = domainProperty.getDomainType();
        key = domainProperty.getForeignKey();

        // config is part of the configuration happening here, so we copy the config map
        if (this.config == null)
        {
            this.config = new HashMap<>();
        }

        final Map<String, Object> config = domainProperty.getConfig();
        if (config != null)
        {
            this.config.putAll(config);
        }

        return this;
    }


    public DomainPropertyBuilder fromMap(Map<String,Object> map)
    {
        name = (String) map.get("name");
        type = (String) map.get("type");
        defaultValue = (String) map.get("defaultValue");
        typeParam = (String) map.get("typeParam");
        domainType = (String) map.get("domainType");
        key = (ForeignKeyDefinition) map.get("foreignKey");

        final Object requiredValue = map.get("required");
        final Object max = map.get("maxLength");
        this.required = requiredValue instanceof Boolean && (Boolean) requiredValue;
        this.maxLength = max instanceof Integer ? (Integer) max : -1;

        // config is part of the configuration happening here, so we copy the config map
        if (this.config == null)
        {
            this.config = new HashMap<>();
        }

        final Map<String, Object> config = (Map<String, Object>) map.get("config");
        if (config != null)
        {
            this.config.putAll(config);
        }
        return this;
    }


    public DomainPropertyBuilder fromJSObject(JSObject map)
    {
        name = (String) map.getMember("name");
        type = (String) map.getMember("type");
        defaultValue = (String) map.getMember("defaultValue");
        typeParam = (String) map.getMember("typeParam");
        domainType = (String) map.getMember("domainType");
        final JSObject foreignKeyValue = (JSObject) map.getMember("foreignKey");
        if (foreignKeyValue != null)
        {
            key = new ForeignKeyDefinition();
            key.setType((String) foreignKeyValue.getMember("type"));
            key.setProperty((String) foreignKeyValue.getMember("property"));
        }
        else
        {
            key = null;
        }

        final Object requiredValue = map.getMember("required");
        final Object max = map.getMember("maxLength");
        this.required = requiredValue instanceof Boolean && (Boolean) requiredValue;
        this.maxLength = max instanceof Integer ? (Integer) max : -1;

        // config is part of the configuration happening here, so we copy the config map
        if (this.config == null)
        {
            this.config = new HashMap<>();
        }

        final JSObject config = (JSObject) map.getMember("config");
        if (config != null)
        {
            for (String name : config.keySet())
            {
                this.config.put(name, config.getMember(name));
            }
        }
        return this;
    }


    public DomainPropertyBuilder withDescription(String description)
    {

        this.description = description;

        return this;
    }
}
