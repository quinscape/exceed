package de.quinscape.exceed.runtime.service.model;


import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates model meta information by finding and reflecting the current model classes
 */
@Service
public class ModelSchemaService
{
    private final static Logger log = LoggerFactory.getLogger(ModelSchemaService.class);

    private final Map<Class<?>,DomainType> modelDomainTypes;

    private final static Map<Class<?>, String> MODEL_CLASS_TO_PROPERTY;
    static
    {
        final HashMap<Class<?>, String> map = new HashMap<>();

        map.put(Boolean.class, "Boolean");
        map.put(Boolean.TYPE, "Boolean");
        map.put(Byte.class, "Integer");
        map.put(Byte.TYPE, "Integer");
        map.put(Short.class, "Integer");
        map.put(Short.TYPE, "Integer");
        map.put(Integer.class, "Integer");
        map.put(Integer.TYPE, "Integer");
        map.put(Long.class, "Long");
        map.put(Long.TYPE, "Long");
        map.put(String.class, "PlainText");
        map.put(Timestamp.class, "Timestamp");
        map.put(Date.class, "Date");
        map.put(Object.class, "Object");

        MODEL_CLASS_TO_PROPERTY = Collections.unmodifiableMap(map);
    }


    public ModelSchemaService()
    {
        modelDomainTypes = new HashMap<>();
    }


    @PostConstruct
    public void init() throws ClassNotFoundException
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(Model.class));

        final Set<BeanDefinition> candidates = provider.findCandidateComponents(Model.class.getPackage()
            .getName());

        for (BeanDefinition candidate : candidates)
        {
            analyze((Class<? extends Model>) Class.forName(candidate.getBeanClassName()));
        }
    }


    private void analyze(Class<?> cls)
    {

        if (modelDomainTypes.containsKey(cls))
        {
            return;
        }

        log.debug("Analyzing {}", cls);

        final String domainTypeName = Model.getType(cls);
        final DomainType domainType = new DomainType();
        domainType.setName(domainTypeName);
        domainType.setSystem(true);
        domainType.setStorageConfiguration(DomainType.SYSTEM_STORAGE);

        modelDomainTypes.put(cls, domainType);

        final JSONClassInfo classInfo = JSONUtil.getClassInfo(cls);

        List<DomainProperty> properties = new ArrayList<>();
        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (info.isIgnore())
            {
                continue;
            }

            final String propName = info.getJsonName();
            final Class<Object> propType = info.getType();
            final Class<Object> typeHint = info.getTypeHint();

            String type = exceedTypeFromPropertyType(propType);
            if (type != null)
            {
                String typeParam = null;
                switch (type)
                {
                    case DomainProperty.MAP_PROPERTY_TYPE:
                    case DomainProperty.LIST_PROPERTY_TYPE:
                        if (typeHint != null)
                        {
                            if (isInModelPackage(typeHint))
                            {
                                typeParam = Model.getType(typeHint);
                                analyze(typeHint);
                            }
                            else
                            {
                                typeParam = exceedTypeFromPropertyType(typeHint);
                            }
                        }
                        else
                        {
                            typeParam = "Object";
                        }
                        break;
                    case DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE:
                        typeParam = Model.getType(propType);
                        analyze(propType);
                        break;
                }

                properties.add(DomainProperty.builder().withName(propName).withType(type).withTypeParam(typeParam)
                    .withDomainType(domainTypeName).build());
            }
        };

        domainType.setProperties(properties);


    }


    public Map<Class<?>, DomainType> getModelDomainTypes()
    {
        return modelDomainTypes;
    }

    public DataGraph createDataGraph(TopLevelModel topLevelModel)
    {
        return new DataGraph(getColumns(topLevelModel), topLevelModel, 1);
    }

    public <T extends TopLevelModel> DataGraph createDataGraph(Class<T> cls, Map<String,T> map)
    {
        return new DataGraph(getMapColumns(cls), map, 1);
    }


    private <T extends TopLevelModel> Map<String, DomainProperty> getMapColumns(Class<T> cls)
    {
        Map<String, DomainProperty> map = new HashMap<>();
        map.put(DataGraph.WILDCARD_SYMBOL, DomainProperty.builder().withName("*").withType(DomainProperty
            .DOMAIN_TYPE_PROPERTY_TYPE).withTypeParam("").build());
        return map;
    }


    private Map<String, DomainProperty> getColumns(TopLevelModel topLevelModel)
    {
        Map<String, DomainProperty> map = new HashMap<>();

        final DomainType domainType = getModelDomainTypes().get(topLevelModel.getClass());
        for (DomainProperty property : domainType.getProperties())
        {
            map.put(property.getName(), property);
        }
        return map;
    }


    private String exceedTypeFromPropertyType(Class<Object> propType)
    {
        final String type = MODEL_CLASS_TO_PROPERTY.get(propType);
        if (type != null)
        {
            return type;
        }
        else if (Map.class.isAssignableFrom(propType))
        {
            return DomainProperty.MAP_PROPERTY_TYPE;
        }
        else if (Collection.class.isAssignableFrom(propType))
        {
            return DomainProperty.LIST_PROPERTY_TYPE;
        }
        else if (isInModelPackage(propType))
        {
            return DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE;
        }
        return null;
    }


    private boolean isInModelPackage(Class<Object> propType)
    {
        return propType.getPackage().getName().startsWith(Model.MODEL_PACKAGE);
    }
}
