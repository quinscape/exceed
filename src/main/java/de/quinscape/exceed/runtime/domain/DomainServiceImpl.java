package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datalist.DataListService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DomainServiceImpl
    implements DomainService
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);

    private final Map<String, PropertyConverter> converters;

    private final StorageConfigurationRepository storageConfigurationRepository;

    private final JSONParser parser;

    private String schema;

    private RuntimeApplication runtimeApplication;

    private DataListService dataListService;


    public DomainServiceImpl(
        Map<String, PropertyConverter> converters,
        StorageConfigurationRepository storageConfigurationRepository
    )
    {
        this.converters = converters;
        this.storageConfigurationRepository = storageConfigurationRepository;

        parser = new JSONParser();
        parser.addObjectFactory(new DomainFactory(this));
        Map<Class, Class> mappings = new HashMap<>();

        mappings.put(Collection.class, ArrayList.class);
        mappings.put(Set.class, HashSet.class);
        mappings.put(List.class, ArrayList.class);
        mappings.put(Map.class, HashMap.class);
        mappings.put(DomainObject.class, GenericDomainObject.class);

        parser.setInterfaceMappings(mappings);
    }


    public void init(RuntimeApplication runtimeApplication, String schema)
    {
        this.runtimeApplication = runtimeApplication;
        this.schema = schema;

        this.dataListService = new DataListService(runtimeApplication.getApplicationModel().getDomainTypes(),
            converters);
    }


    @Override
    public String toJSON(Object domainObject)
    {
        return dataListService.toJSON(domainObject);
    }


    /**
     * Converts the given model JSON to a model instance.
     *
     * @param cls  class to parse into
     * @param json JSON string. Must have a root "type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public <T> T toDomainObject(Class<T> cls, String json)
    {
        return parser.parse(cls, json);
    }


    @Override
    public DomainType getDomainType(String name)
    {
        return runtimeApplication.getApplicationModel().getDomainType(name);
    }


    @Override
    public String getSchema()
    {
        return schema;
    }


    @Override
    public Map<String, DomainType> getDomainTypes()
    {
        return runtimeApplication.getApplicationModel().getDomainTypes();
    }


    private class DomainFactory
        implements org.svenson.ObjectFactory<GenericDomainObject>
    {
        private final DomainService domainService;


        private DomainFactory(DomainService domainService)
        {
            if (domainService == null)
            {
                throw new IllegalArgumentException("domainService can't be null");
            }

            this.domainService = domainService;
        }


        @Override
        public boolean supports(Class<GenericDomainObject> cls)
        {
            return DomainObjectBase.class.isAssignableFrom(cls);
        }


        @Override
        public GenericDomainObject create(Class<GenericDomainObject> cls)
        {
            try
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Create {}", cls.getName());
                }
                GenericDomainObject domainObject = new GenericDomainObject();
                domainObject.setDomainService(domainService);
                return domainObject;
            }
            catch (Exception e)
            {
                throw new DomainObjectCreationException("Error creating domain object", e);
            }
        }
    }


    @Override
    public Map<String, EnumType> getEnums()
    {
        return runtimeApplication.getApplicationModel().getEnums();
    }


    @Override
    public DomainObject create(String type, String id)
    {
        return dbOps(type).create(this, type, id);
    }


    @Override
    public DomainObject read(String type, String id)
    {
        return dbOps(type).read(this, type, id);
    }


    @Override
    public void delete(DomainObject genericDomainObject)
    {
        dbOps(genericDomainObject.getDomainType()).delete(this, genericDomainObject);
    }


    @Override
    public void insert(DomainObject genericDomainObject)
    {
        dbOps(genericDomainObject.getDomainType()).insert(this, genericDomainObject);
    }


    @Override
    public void insertOrUpdate(DomainObject genericDomainObject)
    {
        dbOps(genericDomainObject.getDomainType()).insertOrUpdate(this, genericDomainObject);
    }


    @Override
    public void update(DomainObject genericDomainObject)
    {
        dbOps(genericDomainObject.getDomainType()).update(this, genericDomainObject);
    }


    private DomainOperations dbOps(String domainType)
    {
        final StorageConfiguration ops = getStorageConfiguration(domainType);

        if (ops == null)
        {
            throw new IllegalArgumentException("Domain operations not supported for type '" + domainType + "'");
        }

        return ops.getDomainOperations();
    }


    @Override
    public PropertyConverter getPropertyConverter(String propertyType)
    {
        String converterName = propertyType + "Converter";
        PropertyConverter propertyConverter = converters.get(converterName);

        if (propertyConverter == null)
        {
            throw new IllegalStateException("No converter '" + converterName + "' for type '" + propertyType + "'");
        }
        return propertyConverter;
    }


    @Override
    public StorageConfiguration getStorageConfiguration(String domainType)
    {
        final DomainType type = runtimeApplication.getApplicationModel().getDomainType(domainType);
        final String config = type.getStorageConfiguration();
        return storageConfigurationRepository.getConfiguration(config);
    }
}
