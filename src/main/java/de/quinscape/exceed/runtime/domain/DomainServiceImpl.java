package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datalist.DataListService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.DBUtil;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DomainServiceImpl
    implements DomainService
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);

    private final DataListService dataListService;

    private final DSLContext dslContext;

    private final NamingStrategy namingStrategy;

    private final JSONParser parser;

    private String schema;

    private RuntimeApplication runtimeApplication;


    public DomainServiceImpl(DataListService dataListService, DSLContext dslContext, NamingStrategy namingStrategy)
    {
        this.dataListService = dataListService;
        this.dslContext = dslContext;
        this.namingStrategy = namingStrategy;

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
     *
     * @param cls       class to parse into
     * @param json      JSON string. Must have a root "type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public Object toDomainObject(Class<?> cls, String json)
    {
        return parser.parse(cls, json);
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
    public Map<String,EnumType> getEnums()
    {
        return runtimeApplication.getApplicationModel().getEnums();
    }


    @Override
    public GenericDomainObject read(String type, Object... pkFields)
    {
        DomainType domainType = getDomainType(type);


        return null;
    }



    @Override
    public void delete(DomainObject domainObject)
    {
        DomainType domainType = getDomainType(domainObject.getType());

        DeleteQuery<Record> query = dslContext.deleteQuery(DBUtil.jooqTableFor(domainType, domainType.getName()));

        for (String name : domainType.getPkFields())
        {
            Field<Object> pkField = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name)));
            Object pkValue = domainObject.getProperty(name);
            query.addConditions(pkField.eq(pkValue));
        }

        int count = query.execute();
        if (count != 1)
        {
            log.warn("Update returned " + count + " results instead of one");
        }
    }


    @Override
    public void insert(DomainObject domainObject)
    {
        DomainType domainType = getDomainType(domainObject.getType());

        InsertQuery<Record> query = dslContext.insertQuery(DBUtil.jooqTableFor(domainType, domainType.getName()));


        for (String name : domainObject.propertyNames())
        {
            Field<Object> field = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name)));
            query.addValue(field,  domainObject.getProperty(name));
        }

        int count = query.execute();
        if (count != 1)
        {
            log.warn("Insert returned " + count + " results instead of one");
        }
    }


    @Override
    public void update(DomainObject domainObject)
    {

        DomainType domainType = getDomainType(domainObject.getType());

        UpdateQuery<Record> query = dslContext.updateQuery(DBUtil.jooqTableFor(domainType, domainType.getName()));

        Set<String> nonPkFields = new HashSet<>(domainObject.propertyNames());
        nonPkFields.removeAll(domainType.getPkFields());

        for (String name : nonPkFields)
        {
            Field<Object> field = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name)));
            query.addValue(field, domainObject.getProperty(name));
        }

        for (String name : domainType.getPkFields())
        {
            Field<Object> pkField = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name)));
            Object pkValue = domainObject.getProperty(name);
            query.addConditions(pkField.eq(pkValue));
        }

        int count = query.execute();
        if (count != 1)
        {
            log.warn("Update returned " + count + " results instead of one");
        }
    }

    @Override
    public NamingStrategy getNamingStrategy()
    {
        return namingStrategy;
    }
}
