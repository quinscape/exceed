package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datalist.DataListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DomainServiceImpl
    implements DomainService
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);


    private final DataListService dataListService;

    private final JSONParser parser;

    private String schema;

    private RuntimeApplication runtimeApplication;


    public DomainServiceImpl(DataListService dataListService)
    {
        this.dataListService = dataListService;

        parser = new JSONParser();
        parser.addObjectFactory(new DomainFactory(this));
    }


    public void init(RuntimeApplication runtimeApplication, String schema)
    {
        this.runtimeApplication = runtimeApplication;
        this.schema = schema;
    }


    @Override
    public String toJSON(Object domainObject)
    {
        return dataListService.toJSON(domainObject);
    }


    /**
     * Converts the given model JSON to a model instance.
     *
     * @param json JSON string. Must have a root "type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public Object toDomainObject(String json)
    {
        return parser.parse(GenericDomainObject.class, json);
    }


    @Override
    public DomainType getDomainType(String name)
    {
        DomainType domainType = runtimeApplication.getApplicationModel().getDomainTypes().get(name);
        if (domainType == null)
        {
            throw new IllegalArgumentException("Unknown type '" + name + "'");
        }

        return domainType;
    }


    @Override
    public String getSchema()
    {
        return schema;
    }


    @Override
    public Set<String> getDomainTypeNames()
    {
        return Collections.unmodifiableSet(runtimeApplication.getApplicationModel().getDomainTypes().keySet());
    }


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
            return GenericDomainObject.class.isAssignableFrom(cls);
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
                GenericDomainObject domainObject = cls.newInstance();

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
    public Map<String,EnumModel> getEnums()
    {
        return runtimeApplication.getApplicationModel().getEnums();
    }
}
