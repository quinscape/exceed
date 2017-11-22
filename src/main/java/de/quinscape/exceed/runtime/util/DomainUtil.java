package de.quinscape.exceed.runtime.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GeneratedDomainObject;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.expression.query.QueryCondition;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import org.jooq.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DomainUtil
{
    private final static Logger log = LoggerFactory.getLogger(DomainUtil.class);


    public static DomainObject copy(RuntimeContext runtimeContext, DomainObject domainObject)
    {
        if (domainObject == null)
        {
            return null;
        }


        DomainObject copy = domainObject.getDomainService().create(runtimeContext, domainObject.getDomainType(), domainObject.getId());
        DomainUtil.copyProperties(runtimeContext, domainObject, copy, false);
        return copy;
    }


    /**
     * Copies the declared properties from one give domain object to another
     *
     * @param runtimeContext        runtime context
     * @param from                  source domain object
     * @param to                    target domain object
     * @param convertToJSON         <code>true</code> if the properties should be converted to their JSON format, otherwise
     *                              they're copied as-is.
     */
    public static void copyProperties(RuntimeContext runtimeContext, DomainObject from, DomainObject to, boolean
        convertToJSON)
    {
        DomainService domainService = from.getDomainService();
        DomainType domainType = domainService.getDomainType(from.getDomainType());

        for (DomainProperty property : domainType.getProperties())
        {
            String name = property.getName();
            Object value = from.getProperty(name);

            if (convertToJSON)
            {
                PropertyConverter propertyConverter = property.getPropertyType();
                value = propertyConverter.convertToJSON(runtimeContext, value);
            }
            to.setProperty(name, value);
        }
    }

    public static DomainObject convertToJava(RuntimeContext runtimeContext, DomainObject domainObject) throws ParseException

    {
        if (domainObject == null)
        {
            return null;
        }

        final DomainService domainService = domainObject.getDomainService();

        DomainType domainType = domainService.getDomainType(domainObject.getDomainType());
        for (DomainProperty property : domainType.getProperties())
        {
            String propertyName = property.getName();
            Object value = domainObject.getProperty(propertyName);
            PropertyConverter propertyConverter = property.getPropertyType();

            if (propertyConverter == null)
            {
                throw new IllegalStateException("No converter for property '" + property.getType() + "'");
            }

            Object converted = propertyConverter.convertToJava(runtimeContext, value);

            domainObject.setProperty(propertyName, converted);
        }

        return domainObject;
    }

    public static DomainObject convertToJava(RuntimeContext runtimeContext, Map<String,Object> domainObjectMap) throws ParseException

    {
        if (domainObjectMap == null)
        {
            return null;
        }

        final DomainService domainService = runtimeContext.getDomainService();

        final String domainTypeName = (String) domainObjectMap.get(DomainType.TYPE_PROPERTY);

        final DomainObject domainObject = domainService.create(runtimeContext, domainTypeName, (String) domainObjectMap.get("id"));

        DomainType domainType = domainService.getDomainType(domainTypeName);
        for (DomainProperty property : domainType.getProperties())
        {
            String propertyName = property.getName();
            Object value = domainObjectMap.get(propertyName);
            PropertyConverter propertyConverter = property.getPropertyType();

            if (propertyConverter == null)
            {
                throw new IllegalStateException("No converter for property '" + property.getType() + "'");
            }

            Object converted = propertyConverter.convertToJava(runtimeContext, value);

            domainObject.setProperty(propertyName, converted);
        }

        return domainObject;
    }


    public static DomainObject convertToJSON(RuntimeContext runtimeContext, DomainObject domainObject)
    {
        final DomainService domainService = domainObject.getDomainService();

        DomainType domainType = domainService.getDomainType(domainObject.getDomainType());

        log.debug("Converting domain object of type '{}'", domainType.getName());

        DomainObject copy = domainObject.getDomainService().create(runtimeContext, domainObject.getDomainType(), domainObject.getId());

        for (DomainProperty property : domainType.getProperties())
        {
            String propertyName = property.getName();
            Object value = domainObject.getProperty(propertyName);
            PropertyConverter propertyConverter = property.getPropertyType();

            if (propertyConverter == null)
            {
                throw new IllegalStateException("No converter for property '" + property.getType() + "'");
            }

            log.debug("Converting property '{}': in = {}", propertyName, value);

            Object converted = propertyConverter.convertToJSON(runtimeContext, value);

            copy.setProperty(propertyName, converted);
        }

        return copy;
    }

    public static <T extends DomainObject>  Map<String,T> mapById(List<T> objects)
    {
        final HashMap<String, T> map = Maps.newHashMapWithExpectedSize(objects.size());

        for (T obj : objects)
        {
            map.put(obj.getId(), obj);
        }
        return map;
    }

    public static DomainObject merge(RuntimeContext runtimeContext, DomainObject partialDomainObject)
    {
        final DomainService domainService = partialDomainObject.getDomainService();

        final String domainType = partialDomainObject.getDomainType();
        final String id = partialDomainObject.getId();

        if (id == null)
        {
            throw new IllegalStateException("Object to be merged contains no id: " + partialDomainObject);
        }

        final DomainType domainTypeModel = domainService.getDomainType(domainType);

        final List<DomainProperty> properties = domainTypeModel.getProperties();

        final Set<String> nullPropNames = Sets.newHashSetWithExpectedSize(properties.size());
        for (DomainProperty property : properties)
        {
            final String propertyName = property.getName();
            if (partialDomainObject.getProperty(propertyName) == null)
            {
                nullPropNames.add(propertyName);
            }
        }

        if (nullPropNames.size() > 0)
        {
            final DomainObject persisted = domainService.read(
                runtimeContext,
                domainType,
                id
            );

            if (persisted != null)
            {
                // copy all null props from the db version
                for (String propertyName : nullPropNames)
                {
                    partialDomainObject.setProperty(propertyName, persisted.getProperty(propertyName));
                }
            }
        }
        
        return partialDomainObject;
    }

    public static List<DomainObject> query(RuntimeContext runtimeContext, String domainTypeName, Condition condition)
    {
        final DomainService domainService = runtimeContext.getDomainService();

        final DomainType domainType = runtimeContext.getApplicationModel().getDomainType(domainTypeName);
        final QueryDomainType queryDomainType = new QueryDomainType(domainType);

        selectDomainTypeFields(queryDomainType, domainType);

        QueryDefinition queryDefinition = new QueryDefinition(queryDomainType);

        queryDefinition.setFilter(
            new QueryCondition(
                condition
            )
        );

        final ExceedDataSource dataSource = domainService.getDataSource(domainType.getDataSourceName());
        final DataGraph graph = dataSource.getStorageConfiguration().getDomainOperations().query(
            runtimeContext,
            domainService,
            queryDefinition
        );

        setDomainTypeInGraphResults(graph, domainTypeName);

        return (List<DomainObject>) graph.getRootCollection();
    }


    /**
     * Makes sure all domain objects in the given array graph have a "_type" field which they normally
     * don't have. This is a shortcut to use the graph objects as full domain objects without extraction.
     *
     * @param graph             array data graph
     * @param domainTypeName    domain type name
     */
    private static void setDomainTypeInGraphResults(DataGraph graph, String domainTypeName)
    {
        final List<DomainObject> domainObjects = (List<DomainObject>) graph.getRootCollection();
        for (DomainObject domainObject : domainObjects)
        {
            if (!(domainObject instanceof GeneratedDomainObject))
            {
                domainObject.setDomainType(domainTypeName);
            }
        }
    }


    /**
     * Selects all properties of the given domain type model in the given query domain type.
     * <p>
     *     By default, the query mechanism will select all fields, but it will select them as
     *     "Type.field" or "alias.field", we need the orignal property names.
     * </p>
     *
     * @param queryDomainType   query domain type
     * @param domainType        domain type model
     */
    public static void selectDomainTypeFields(QueryDomainType queryDomainType, DomainType domainType)
    {
        final List<String> fields = domainType.getProperties()
            .stream()
            .map(PropertyModel::getName)
            .collect(Collectors.toList());

        queryDomainType.selectedFields(
            fields
        );
    }


    public static DomainObject queryOne(RuntimeContext runtimeContext, String domainTypeName, Condition condition)
    {
        final List<DomainObject> rows = query(runtimeContext, domainTypeName, condition);

        if (rows.size() > 1)
        {
            throw new IllegalStateException("More than one result: " + rows);
        }

        return rows.size() > 0 ? rows.get(0): null;
    }
}
