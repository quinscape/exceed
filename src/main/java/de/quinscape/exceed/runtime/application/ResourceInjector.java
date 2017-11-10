package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.annotation.InjectResource;
import de.quinscape.exceed.model.annotation.ResourceInjectorPredicate;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.svenson.JSONParseException;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import javax.script.CompiledScript;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Injects the contents of exceed resources into the meta data object based on the @{@link InjectResource} annotations.
 */
public class ResourceInjector
{
    private final static Logger log = LoggerFactory.getLogger(ResourceInjector.class);

    private final List<MetaDataResourceProperty> properties;

    private final Class<?> targetClass;

    private final Map<String, ResourceInjectorPredicate> predicates;


    /**
     * Creates a new ResourceInjector instance.
     *
     * @param targetClass   Type this injector uses as target
     * @param predicates
     */
    public ResourceInjector(
        Class<?> targetClass,
        Map<String, ResourceInjectorPredicate> predicates
    )
    {
        this.targetClass = targetClass;
        this.predicates = predicates;
        properties = analyze(targetClass);
    }


    public List<MetaDataResourceProperty> getProperties()
    {
        return properties;
    }

    private List<MetaDataResourceProperty> analyze(Class<?> cls)
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(cls);
        List<MetaDataResourceProperty> list = new ArrayList<>();
        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            final InjectResource anno = JSONUtil.findAnnotation(info, InjectResource.class);
            if (anno != null)
            {
                final Method setter = ((JavaObjectPropertyInfo) info).getSetterMethod();
                final String beanName = anno.predicate();

                final ResourceInjectorPredicate predicate;
                if (beanName.length() > 0)
                {
                    predicate = lookup(beanName);
                }
                else
                {
                    predicate = null;
                }

                list.add(new MetaDataResourceProperty(setter, anno.value(), predicate));
            }
        }

        return list;
    }


    private ResourceInjectorPredicate lookup(String beanName)
    {
        final ResourceInjectorPredicate predicate = predicates.get(beanName);
        if (predicate == null)
        {
            throw new IllegalStateException("No ResourceInjectorPredicate with name '" + beanName + "' found among spring beans");
        }
        return predicate;
    }


    /**
     * Injects the current state of all resources into the meta data object.
     *
     * @param nashorn           nashorn engine
     * @param resourceLoader    resource loader
     * @param target            meta data
     *
     * @throws IllegalArgumentException if the given target object is not of the required type.
     */
    public void injectResources(RuntimeContext runtimeContext, NashornScriptEngine nashorn, ResourceLoader resourceLoader, Object target)
    {
        if (!target.getClass().equals(targetClass))
        {
            throw new IllegalArgumentException("Target instance is not an instance of " + targetClass);
        }

        for (MetaDataResourceProperty property : properties)
        {
            updateResourceInternal(runtimeContext, nashorn, resourceLoader, target, property);
        }
    }


    /**
     * Reinjects the resource contents for the given resource path. If the given resource path is not one of the annotated
     * values, this method will do nothing.
     *
     * @param nashorn           nashorn
     * @param resourceLoader    resource loader
     * @param target          meta data
     * @param resourcePath      resource path
     */
    public void updateResource(RuntimeContext runtimeContext, NashornScriptEngine nashorn, ResourceLoader resourceLoader, Object target, String resourcePath)
    {

        for (MetaDataResourceProperty property : properties)
        {
            if (property.resourcePath.equals(resourcePath))
            {
                updateResourceInternal(runtimeContext, nashorn, resourceLoader, target, property);
                return;
            }
        }
    }

    private void updateResourceInternal(
        RuntimeContext runtimeContext, NashornScriptEngine nashorn,
        ResourceLoader resourceLoader, Object target, MetaDataResourceProperty property
    )
    {
        if (property.predicate != null && !property.predicate.shouldInject(runtimeContext))
        {
            return;
        }

        final String resourcePath = property.resourcePath;
        log.debug("Updating meta data resource {}", resourcePath);
        
        try
        {
            final PathResources resourceLocation = resourceLoader.getResources(resourcePath);

            if (resourceLocation == null)
            {
                throw new IllegalStateException("No resources for location '" + resourcePath + "'");
            }

            String content = new String(
                resourceLocation.getHighestPriorityResource().read(),
                RequestUtil.UTF_8
            );

            Class<?> type = property.setter.getParameterTypes()[0];

            if (CompiledScript.class.isAssignableFrom(type))
            {
                property.setter.invoke(target, nashorn.compile(content));
            }
            else if (type.equals(String.class))
            {
                property.setter.invoke(target, content);
            }
            else
            {
                try
                {
                    final Object value = JSONUtil.DEFAULT_PARSER.parse(type, content);
                    property.setter.invoke(target, value);
                }
                catch(JSONParseException e)
                {
                    log.info("Ignoring invalid JSON resource change for " + resourceLocation + " contains: {}", e.getMessage());
                }
            }
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException("Error setting resource contents via " + property.setter, e);
        }
    }

    /**
     * Meta data for @InjectResource annotated target class properties
     */
    private static class MetaDataResourceProperty
    {
        public final Method setter;
        public final String resourcePath;
        public final ResourceInjectorPredicate predicate;


        private MetaDataResourceProperty(
            Method setter,
            String resourcePath,
            ResourceInjectorPredicate predicate
        )
        {
            this.setter = setter;
            this.resourcePath = resourcePath;
            this.predicate = predicate;
        }


        @Override
        public String toString()
        {
            return super.toString() + ": "
                + "setter = " + setter
                + ", resourcePath = '" + resourcePath + '\''
                ;
        }
    }
}
