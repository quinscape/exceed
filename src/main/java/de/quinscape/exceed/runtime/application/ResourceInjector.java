package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.annotation.InjectResource;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParseException;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import javax.script.CompiledScript;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Injects the contents of exceed resources into the meta data object based on the @{@link InjectResource} annotations.
 */
public class ResourceInjector
{
    private final static Logger log = LoggerFactory.getLogger(ResourceInjector.class);

    private final List<MetaDataResourceProperty> properties;

    private final Class<?> targetClass;


    /**
     * Creates a new ResourceInjector instance.
     *
     * @param targetClass   Type this injector uses as target
     */
    public ResourceInjector(Class<?> targetClass)
    {
        this.targetClass = targetClass;
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
            final InjectResource metaDataResource = JSONUtil.findAnnotation(info, InjectResource.class);
            if (metaDataResource != null)
            {
                final Method setter = ((JavaObjectPropertyInfo) info).getSetterMethod();
                list.add(new MetaDataResourceProperty(setter, metaDataResource.value()));
            }
        }

        return list;
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
    public void injectResources(NashornScriptEngine nashorn, ResourceLoader resourceLoader, Object target)
    {
        if (!target.getClass().equals(targetClass))
        {
            throw new IllegalArgumentException("Target instance is not an instance of " + targetClass);
        }

        for (MetaDataResourceProperty property : properties)
        {
            updateResource(nashorn, resourceLoader, target, property);
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
    public void updateResource(NashornScriptEngine nashorn, ResourceLoader resourceLoader, Object target, String resourcePath)
    {

        for (MetaDataResourceProperty property : properties)
        {
            if (property.resourcePath.equals(resourcePath))
            {
                updateResource(nashorn, resourceLoader, target, property);
                return;
            }
        }
    }

    private void updateResource(NashornScriptEngine nashorn, ResourceLoader resourceLoader, Object target, MetaDataResourceProperty property)
    {
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

        private MetaDataResourceProperty(Method setter, String resourcePath)
        {
            this.setter = setter;
            this.resourcePath = resourcePath;
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
