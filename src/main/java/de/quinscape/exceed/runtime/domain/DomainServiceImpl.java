package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainProperty;
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
import org.jooq.RecordMapper;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DomainServiceImpl
    implements DomainService
{
    private final static Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);


    private final DSLContext dslContext;

    private final NamingStrategy namingStrategy;

    private final Map<String, PropertyConverter> converters;

    private final JSONParser parser;

    private String schema;

    private RuntimeApplication runtimeApplication;

    private DataListService dataListService;


    public DomainServiceImpl(
        DSLContext dslContext, 
        NamingStrategy namingStrategy,
        Map<String, PropertyConverter> converters,
        StorageConfigurationRepository storageConfigurationRepository
    )
    {
        this.dslContext = dslContext;
        this.namingStrategy = namingStrategy;
        this.converters = converters;

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
     *
     * @param cls       class to parse into
     * @param json      JSON string. Must have a root "type" property that contains a valid model name,
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
    public DomainObject create(String type, String id)
    {
        GenericDomainObject genericDomainObject = new GenericDomainObject();
        genericDomainObject.setDomainType(type);
        genericDomainObject.setId(id);
        genericDomainObject.setDomainService(this);

        return genericDomainObject;
    }

    @Override
    public DomainObject read(String type, String id)
    {
        DomainType domainType = getDomainType(type);

        Table<Record> table = DBUtil.jooqTableFor(domainType, domainType.getName());
        Field<Object> idField = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), "id")));

        DomainObject genericDomainObject = dslContext.select()
            .from(table)
            .where(idField.eq(id))
            .fetchOne(new DomainTypeRecordMapper(domainType));
        return genericDomainObject;
    }

    @Override
    public void delete(DomainObject domainObject)
    {
        DomainType domainType = getDomainType(domainObject.getDomainType());

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
        DomainType domainType = getDomainType(domainObject.getDomainType());

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

        DomainType domainType = getDomainType(domainObject.getDomainType());

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
    public PropertyConverter getPropertyConverter(String name)
    {
        String converterName = name + "Converter";
        PropertyConverter propertyConverter = converters.get(converterName);

        if (propertyConverter == null)
        {
            throw new IllegalStateException("No converter '" + converterName + "' for type '" + name + "'");
        }
        return propertyConverter;
    }


    @Override
    public NamingStrategy getNamingStrategy()
    {
        return namingStrategy;
    }


    private class DomainTypeRecordMapper
        implements RecordMapper
        <Record,DomainObject>
    {
        private final DomainType domainType;


        private DomainTypeRecordMapper(DomainType domainType)
        {
            this.domainType = domainType;
        }


        @Override
        public DomainObject map(Record record)
        {
            DomainService domainService = domainType.getDomainService();
            DomainObject domainObject = domainService.create(domainType.getName(), null);
            for (DomainProperty property : domainType.getProperties())
            {
                String name = property.getName();
                Object value = record.getValue(DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name))));
                domainObject.setProperty(name, value);
            }
            return domainObject;
        }
    }
}
