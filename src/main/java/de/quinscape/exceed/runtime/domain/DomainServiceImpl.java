package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datalist.DataGraphService;
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

    private final Map<Class<?>, DomainType> modelDomainTypes;

    private final JSONParser parser;

    private String schema;

    private RuntimeApplication runtimeApplication;

    private DataGraphService dataGraphService;

    private Map<String, DomainType> domainTypes;


    public DomainServiceImpl(
        Map<String, PropertyConverter> converters,
        StorageConfigurationRepository storageConfigurationRepository,
        Map<Class<?>, DomainType> modelDomainTypes)
    {
        this.converters = converters;
        this.storageConfigurationRepository = storageConfigurationRepository;
        this.modelDomainTypes = modelDomainTypes;

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

        final ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        final Map<String, DomainType> domainTypes = new HashMap<>(applicationModel.getDomainTypes());

        modelDomainTypes.values().forEach( domainType ->
            domainTypes.put(domainType.getName(), domainType)
        );

        this.domainTypes = domainTypes;

        this.dataGraphService = new DataGraphService(this, converters);
    }


    @Override
    public String toJSON(Object domainObject)
    {
        return dataGraphService.toJSON(domainObject);
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
        final DomainType domainType = domainTypes.get(name);
        if (domainType == null)
        {
            throw new DomainTypeNotFoundException("Domain type '" + name + "' not found");
        }
        return domainType;
    }


    @Override
    public String getSchema()
    {
        return schema;
    }


    @Override
    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypes;
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
        final DomainType type = domainTypes.get(domainType);
        final String config = type.getStorageConfiguration();
        return storageConfigurationRepository.getConfiguration(config);
    }

    public String getAppName()
    {
        return this.runtimeApplication.getName();
    }
}
