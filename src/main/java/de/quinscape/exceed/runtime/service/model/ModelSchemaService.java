package de.quinscape.exceed.runtime.service.model;


import de.quinscape.exceed.model.AbstractModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.ExceedPropertyType;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.DomainPropertyBuilder;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.property.EnumConverter;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private Map<Class<? extends Enum>, EnumType> modelEnumTypes;

    private final static Map<Class<?>, String> MODEL_CLASS_TO_PROPERTY;
    static
    {
        final HashMap<Class<?>, String> map = new HashMap<>();

        map.put(Boolean.class, PropertyType.BOOLEAN);
        map.put(Boolean.TYPE, PropertyType.BOOLEAN);
        map.put(Byte.class, PropertyType.INTEGER);
        map.put(Byte.TYPE, PropertyType.INTEGER);
        map.put(Short.class, PropertyType.INTEGER);
        map.put(Short.TYPE, PropertyType.INTEGER);
        map.put(Integer.class, PropertyType.INTEGER);
        map.put(Integer.TYPE, PropertyType.INTEGER);
        map.put(Long.class, PropertyType.LONG);
        map.put(Long.TYPE, PropertyType.LONG);
        map.put(String.class, PropertyType.PLAIN_TEXT);
        map.put(Timestamp.class, PropertyType.TIMESTAMP);
        map.put(Date.class, PropertyType.DATE);
        map.put(Object.class, PropertyType.OBJECT);
        map.put(DomainObject.class, PropertyType.DOMAIN_TYPE);

        MODEL_CLASS_TO_PROPERTY = Collections.unmodifiableMap(map);
    }




    public ModelSchemaService()
    {
        modelDomainTypes = new HashMap<>();
        modelEnumTypes = new HashMap<>();
    }


    @PostConstruct
    public void init() throws ClassNotFoundException
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(AbstractModel.class));

        final Set<BeanDefinition> candidates = provider.findCandidateComponents(AbstractModel.class.getPackage()
            .getName());

        for (BeanDefinition candidate : candidates)
        {
            analyze((Class<? extends Model>) Class.forName(candidate.getBeanClassName()));
        }
    }


    private void analyze(Class<?> cls)
    {

        if (modelDomainTypes.containsKey(cls) || modelEnumTypes.containsKey(cls))
        {
            return;
        }

        if (cls.isInterface())
        {
            return;
        }


        if (Enum.class.isAssignableFrom(cls))
        {
            analyzeEnum((Class<? extends Enum>) cls);
        }
        else
        {
            analyzeModel(cls);
        }

    }


    private void analyzeModel(Class<?> cls)
    {
        log.debug("Analyzing model {}", cls);

        final String domainTypeName = Model.getType(cls);
        final DomainTypeModel domainType = new DomainTypeModel();
        domainType.setName(domainTypeName);
        domainType.setSystem(true);
        domainType.setStorageConfiguration(DomainType.SYSTEM_STORAGE);

        modelDomainTypes.put(cls, domainType);

        try
        {
            final JSONClassInfo classInfo = JSONUtil.getClassInfo(cls);


            List<DomainProperty> properties = new ArrayList<>();
            for (JSONPropertyInfo info : classInfo.getPropertyInfos())
            {
                final String propName = info.getJsonName();

                // we ignore all ignored and unreadable properties and also the "type" property because we use the
                // domain type "_type" for the model domain object view.
                if (info.isIgnore() || !info.isReadable() || (AbstractModel.class.isAssignableFrom(cls) && propName.equals("type")))
                {
                    continue;
                }

                final Class<Object> propType = info.getType();
                final Class<Object> typeHint = info.getTypeHint();

                final ExceedPropertyType typeAnno = JSONUtil.findAnnotation(info, ExceedPropertyType.class);

                String type;
                if (typeAnno != null)
                {
                    final DomainPropertyBuilder builder = DomainProperty.builder()
                        .withName(propName)
                        .withType(typeAnno.type(), typeAnno.typeParam().length() > 0 ? typeAnno.typeParam() : null)
                        .withDomainType(domainTypeName);

                    properties.add(builder.build());
                }
                else
                {
                    type = exceedTypeFromPropertyType(propType);
                    if (type != null)
                    {
                        String typeParam = null;
                        switch (type)
                        {
                            case PropertyType.MAP:
                            case PropertyType.LIST:
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
                                    typeParam = PropertyType.OBJECT;
                                }
                                break;
                            case PropertyType.DOMAIN_TYPE:
                            case PropertyType.ENUM:
                                typeParam = Model.getType(propType);
                                analyze(propType);
                                break;
                        }

                        final DomainPropertyBuilder builder = DomainProperty.builder()
                            .withName(propName)
                            .withType(type, typeParam)
                            .withDomainType(domainTypeName);

                        if (PropertyType.ENUM.equals(type))
                        {
                            builder.withConfig(EnumConverter.JAVA_ENUM_CONFIG, propType.getName());
                        }

                        properties.add(builder.build());
                    }
                }

            };
            domainType.setProperties(properties);

        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException("Error analyzing " + cls, e);
        }
    }


    private void analyzeEnum(Class<? extends Enum> cls)
    {
        log.debug("Analyzing enum {}", cls);

        final String enumTypeName = Model.getType(cls);

        EnumType enumType = new EnumType();
        enumType.setName(enumTypeName);
        enumType.setValues(getEnumValues(cls));

        modelEnumTypes.put(cls, enumType);

    }


    private static List<String> getEnumValues(Class<? extends Enum> cls)
    {
        try
        {
            Method valuesMethod = cls.getMethod("values");
            Enum[] values = (Enum[]) valuesMethod.invoke(null);

            List<String> names = new ArrayList<>(values.length);
            for (Enum value : values)
            {
                names.add(value.name());
            }
            return names;
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    public Map<Class<?>, DomainType> getModelDomainTypes()
    {
        return modelDomainTypes;
    }

    public DataGraph createDataGraph(TopLevelModel topLevelModel)
    {
        return new DataGraph(getColumns(topLevelModel), topLevelModel, 1, null);
    }

//    public <T extends TopLevelModel> DataGraph createDataGraph(Class<T> cls, Map<String,T> map)
//    {
//        return new DataGraph(getMapColumns(cls), map, 1);
//    }
//
//
//    private <T extends TopLevelModel> Map<String, DomainProperty> getMapColumns(Class<T> cls)
//    {
//        Map<String, DomainProperty> map = new HashMap<>();
//        map.put(DataGraph.WILDCARD_SYMBOL, DomainProperty.builder().withName("*").withType(PropertyType
//            .DOMAIN_TYPE).withTypeParam("").build());
//        return map;
//    }


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
            return PropertyType.MAP;
        }
        else if (Collection.class.isAssignableFrom(propType))
        {
            return PropertyType.LIST;
        }
        else if (isInModelPackage(propType))
        {
            return propType.isEnum() ? PropertyType.ENUM : PropertyType.DOMAIN_TYPE;
        }
        return null;
    }


    private boolean isInModelPackage(Class<Object> propType)
    {
        return propType.getPackage().getName().startsWith(Model.MODEL_PACKAGE);
    }


    public Map<Class<? extends Enum>, EnumType> getModelEnumTypes()
    {
        return modelEnumTypes;
    }


    public void setModelEnumTypes(Map<Class<? extends Enum>, EnumType> modelEnumTypes)
    {
        this.modelEnumTypes = modelEnumTypes;
    }
}
