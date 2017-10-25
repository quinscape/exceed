package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.AbstractModel;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.ForeignKeyDefinition;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;
import org.svenson.util.JSONBuilder;

public class ModelJSONServiceImpl
    implements ModelJSONService
{

    public static final String MODEL_ATTR_NAME = "model";

    private final static Logger log = LoggerFactory.getLogger(ModelJSONServiceImpl.class);

    private final JSONParser parser;

    private final JSON generator = JSONUtil.DEFAULT_GENERATOR;

    private ClientViewJSONGenerator clientViewJSONGenerator;


    @Required
    public void setClientViewJSONGenerator(ClientViewJSONGenerator clientViewJSONGenerator)
    {
        this.clientViewJSONGenerator = clientViewJSONGenerator;
    }


    public ModelJSONServiceImpl()
    {
        parser = new JSONParser();
        parser.setTypeMapper(new ModelMapper());
    }


    @Override
    public String toJSON(Object model)
    {
        if (model instanceof View)
        {
            return toJSON(null, (View) model, JSONFormat.EXTERNAL);
        }
        else if (model instanceof DomainType)
        {
            return toJSON(null, (DomainType) model, JSONFormat.EXTERNAL);
        }
        return generator.forValue(model);
    }

    @Override
    public String toJSON(ApplicationModel applicationModel, View model, JSONFormat jsonFormat)
    {
        if (jsonFormat == null)
        {
            throw new IllegalArgumentException("jsonFormat can't be null");
        }

        return clientViewJSONGenerator.toJSON(applicationModel, model, jsonFormat);
    }


    @Override
    public String toJSON(ApplicationModel applicationModel, DomainType model, JSONFormat jsonFormat)
    {
        JSONBuilder b = JSONBuilder.buildObject();

        b.propertyUnlessNull("identityGUID", model.getIdentityGUID());
        b.propertyUnlessNull("versionGUID", model.getVersionGUID());
        b.property("name", model.getName());
        b.propertyUnlessNull("storage", model.getStorageConfiguration());
        b.arrayProperty("properties");

        for (DomainProperty domainProperty : model.getProperties())
        {
            b.objectElement();
            b.property("name", domainProperty.getName());
            b.property("type", domainProperty.getType());
            b.propertyUnlessNull("typeParam", domainProperty.getTypeParam());

            if (domainProperty.isRequired())
            {
                b.property("required", true);
            }

            final int maxLength = domainProperty.getMaxLength();
            if (maxLength > 0)
            {
                b.property("maxLength", maxLength);
            }

            b.propertyUnlessNull("defaultValue", domainProperty.getDefaultValue());

            final ForeignKeyDefinition foreignKey = domainProperty.getForeignKey();
            if (foreignKey != null)
            {
                b.objectProperty("foreignKey");
                b.property("type", foreignKey.getType());
                final String property = foreignKey.getProperty();
                if (!property.equals(DomainType.ID_PROPERTY))
                {
                    b.property("property", property);
                }
                b.close();
            }
            b.close();
        }
        return b.output();
    }


    /**
     * Converts the given model JSON to a model instance and validates it to be an expected type.
     *
     * @param cls  Expected (super) class.
     * @param json JSON string. Must have a root "type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public <M> M toModel(Class<M> cls, String json) throws IllegalArgumentException
    {
        try
        {
            Object model = parser.parse((Class)cls, json);
            if (!cls.isInstance(model))
            {
                throw new IllegalArgumentException("Expected " + cls.getSimpleName() + " but got " + json);
            }
            return (M) model;
        }
        catch (IllegalArgumentException e)
        {
            throw new ExceedRuntimeException("Error converting model JSON" + json, e);
        }
    }


    public static class ModelMapper
        extends AbstractPropertyValueBasedTypeMapper
    {
        public ModelMapper()
        {
            setDiscriminatorField("type");
            setPathMatcher(new SubtypeMatcher(AbstractModel.class));
        }


        @Override
        protected Class getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            if (o == null)
            {
                return null;
            }
            return Model.getType((String)o);
        }
    }

}
