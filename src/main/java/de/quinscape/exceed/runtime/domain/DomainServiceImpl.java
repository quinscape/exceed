package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datalist.DataGraphService;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.util.JSONUtil;
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

    private final StorageConfigurationRepository storageConfigurationRepository;

    private final JSONParser parser;

    private String schema;

    private RuntimeApplication runtimeApplication;

    private DataGraphService dataGraphService;

    private Map<String, DomainType> domainTypes;


    public DomainServiceImpl(
        StorageConfigurationRepository storageConfigurationRepository
    )
    {
        this.storageConfigurationRepository = storageConfigurationRepository;

        parser = new JSONParser();
        parser.setObjectSupport(JSONUtil.OBJECT_SUPPORT);
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

        this.domainTypes = domainTypes;

        this.dataGraphService = new DataGraphService(this);
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
    public Map<String, PropertyTypeModel> getPropertyTypes()
    {
        return runtimeApplication.getApplicationModel().getPropertyTypes();
    }


    @Override
    public Map<String, StateMachine> getStateMachines()
    {
        return runtimeApplication.getApplicationModel().getStateMachines();
    }


    @Override
    public DomainObject create(RuntimeContext runtimeContext, String type, String id)
    {
        return dbOps(type).create(runtimeContext, this, type, id, GenericDomainObject.class);
    }

    @Override
    public DomainObject create(RuntimeContext runtimeContext, String type, String id, Class<? extends DomainObject> implClass)
    {
        return dbOps(type).create(runtimeContext, this, type, id, implClass);
    }


    @Override
    public DomainObject read(RuntimeContext runtimeContext, String type, String id)
    {
        return dbOps(type).read(runtimeContext, this, type, id);
    }


    @Override
    public boolean delete(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {
        return dbOps(genericDomainObject.getDomainType()).delete(runtimeContext, this, genericDomainObject);
    }


    @Override
    public void insert(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {
        dbOps(genericDomainObject.getDomainType()).insert(runtimeContext, this, genericDomainObject);
    }


    @Override
    public void insertOrUpdate(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {
        dbOps(genericDomainObject.getDomainType()).insertOrUpdate(runtimeContext, this, genericDomainObject);
    }


    @Override
    public boolean update(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {
        return dbOps(genericDomainObject.getDomainType()).update(runtimeContext, this, genericDomainObject);
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
    public StorageConfiguration getStorageConfiguration(String domainType)
    {
        final DomainType type = getDomainType(domainType);
        final String config = type.getStorageConfiguration();
        return storageConfigurationRepository.getConfiguration(config);
    }


    @Override
    public JsEnvironment getJsEnvironment()
    {
        return runtimeApplication.getApplicationModel().getMetaData().getJsEnvironment();
    }


    public void update(QueryTypeModel queryTypeModel)
    {
        domainTypes.put(queryTypeModel.getName(), queryTypeModel);
    }


    public String getAppName()
    {
        return this.runtimeApplication.getName();
    }
}
