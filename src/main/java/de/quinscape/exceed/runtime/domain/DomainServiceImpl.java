package de.quinscape.exceed.runtime.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DomainServiceImpl
    implements DomainService
{
    private static Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);

    private JSONParser parser;

    private Map<String,Class<? extends DomainObject>> domainTypes;
    private Map<String,Class<? extends DomainObject>> domainTypesRO;

    private Registry registry = new Registry(this);

    public DomainServiceImpl()
    {
        parser = new JSONParser();
        parser.setTypeMapper(new DomainMapper());
        parser.addObjectFactory(new DomainFactory(this));

        domainTypes = new HashMap<>();
        domainTypesRO = Collections.unmodifiableMap(domainTypes);
    }

    @Override
    public <D extends DomainObject> String toJSON(D domainObject)
    {
        return JSON.defaultJSON().forValue(domainObject);
    }

    /**
     * Converts the given model JSON to a model instance.
     *
     * @param json JSON string. Must have a root "_type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public DomainObject toDomainObject(String json)
    {
        return parser.parse(DomainObject.class, json);
    }

    /**
     * Converts the given model JSON to a model instance and validates it to be an expected type.
     *
     * @param cls  Expected (super) class.
     * @param json JSON string. Must have a root "_type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public <D extends DomainObject> D toDomainObject(Class<D> cls, String json)
    {
        DomainObject domainObject = parser.parse(DomainObject.class, json);
        if (!cls.isInstance(domainObject))
        {
            throw new IllegalArgumentException("Expected " + cls.getSimpleName() + " but got " + json);
        }
        return (D) domainObject;
    }

    Map<String,Class<? extends DomainObject>> getDomainTypes()
    {
        return domainTypes;
    }

    Map<String, Class<? extends DomainObject>> getDomainTypesRO()
    {
        return domainTypesRO;
    }

    public class DomainMapper
        extends AbstractPropertyValueBasedTypeMapper
    {
        public DomainMapper()
        {
            setDiscriminatorField("_type");
            setPathMatcher(new SubtypeMatcher(DomainObject.class));
        }

        @Override
        protected Class getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            Class<? extends DomainObject> cls = domainTypes.get(o.toString());
            if (cls == null)
            {
                throw new DomainTypeNameNotFoundException("Cannot create domain type for unknown name '" + o + "'");
            }
            return cls;
        }
    }

    private static class DomainFactory
        implements org.svenson.ObjectFactory<DomainObject>
    {
        private final DomainService domainJSONService;

        private DomainFactory(DomainService domainJSONService)
        {
            if (domainJSONService == null)
            {
                throw new IllegalArgumentException("domainService can't be null");
            }

            this.domainJSONService = domainJSONService;
        }

        @Override
        public boolean supports(Class<DomainObject> cls)
        {
            return DomainObject.class.isAssignableFrom(cls);
        }

        @Override
        public DomainObject create(Class<DomainObject> cls)
        {
            try
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Create {}", cls.getName());
                }
                DomainObject domainObject = cls.newInstance();
                domainObject.setDomainService(domainJSONService);
                return domainObject;
            }
            catch (Exception e)
            {
                throw new DomainObjectCreationException("Error creating domain object", e);
            }
        }
    }

    private static class Registry
        implements DomainRegistry
    {
        private final DomainServiceImpl domainJSONService;

        public Registry(DomainServiceImpl domainJSONService)
        {
            this.domainJSONService = domainJSONService;
        }

        @Override
        public void register(String name, Class<? extends DomainObject> cls) throws DomainTypeNameCollisionException
        {
            Map<String, Class<? extends DomainObject>> domainTypes = domainJSONService.getDomainTypes();

            Class<? extends DomainObject> existingRegistration = domainTypes.get(name);
            if (existingRegistration != null)
            {
                throw new DomainTypeNameCollisionException("Domain Type '" + name + "' is already defined.");
            }
            domainTypes.put(name, cls);
        }

        @Override
        public void override(String name, Class<? extends DomainObject> cls) throws DomainTypeNameNotFoundException, DomainTypeOverridingException
        {
            Map<String, Class<? extends DomainObject>> domainTypes = domainJSONService.getDomainTypes();
            Class<? extends DomainObject> existingRegistration = domainTypes.get(name);
            if (existingRegistration == null)
            {
                throw new DomainTypeNameNotFoundException("Cannot override non-existing domain Type '" + name + "'.");
            }
            if (!existingRegistration.isAssignableFrom(cls))
            {
                throw new DomainTypeOverridingException("Cannot override domain type, " + cls + " does not extend " + existingRegistration + "'.");
            }

            domainTypes.put(name, cls);
        }

        @Override
        public Map<String, Class<? extends DomainObject>> registrations()
        {
            return domainJSONService.getDomainTypesRO();
        }
    }

    @Override
    public DomainRegistry getRegistry()
    {
        return registry;
    }
}
