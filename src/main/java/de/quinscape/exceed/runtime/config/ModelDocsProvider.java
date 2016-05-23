package de.quinscape.exceed.runtime.config;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.annotation.IncludeDocs;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.util.Util;
import de.quinscape.exceed.runtime.view.DataProviderContext;
import de.quinscape.exceed.tooling.GenerateModelDocs;
import de.quinscape.exceed.tooling.JavaDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.svenson.TypeAnalyzer;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectSupport;
import org.svenson.info.ObjectSupport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ModelDocsProvider
    implements DataProvider
{
    private final static Logger log = LoggerFactory.getLogger(ModelDocsProvider.class);

    private final File sourceDir;


    private ObjectSupport javaSupport = new JavaObjectSupport();

    private final Set<String> modelTypes;


    private final static ConcurrentMap<Class<?>,DocsHolder> holders = new ConcurrentHashMap<>();

    public ModelDocsProvider()
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);

        provider.addIncludeFilter(new AssignableTypeFilter(Model.class));

        Set<BeanDefinition> candidates = provider.findCandidateComponents(Model.class.getPackage().getName());

        modelTypes = new HashSet<>();

        for (BeanDefinition candidate : candidates)
        {
            modelTypes.add(candidate.getBeanClassName());
        }
        log.info("Model Types:", modelTypes);


        File libDir = Util.getExceedLibrarySource();
        if (libDir != null)
        {
            sourceDir = new File(libDir, Util.path("src/main/java"));
            if (!sourceDir.exists() || !sourceDir.isDirectory())
            {
                throw new IllegalStateException(sourceDir + " is no existing directory");
            }
        }
        else
        {
            sourceDir = null;
        }
    }

    @Override
    public Map<String, Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel,
                                       Map<String, Object> vars)
    {
        try
        {
            String type = (String) vars.get("type");

            log.debug("type: {}", type);

            if (!type.startsWith("de.quinscape.exceed"))
            {
                return Collections.emptyMap();
            }

            Class<?> cls = Class.forName(type);
            Set<Class<?>> known = new HashSet<>();
            TypeInfo typeInfo = getTypeInfo(cls, known);
            return ImmutableMap.of(
                "typeInfo", typeInfo,
                "types", modelTypes
            );
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private TypeInfo getTypeInfo(Class<?> cls, Set<Class<?>> known) throws IOException
    {
        JSONClassInfo classInfo = TypeAnalyzer.getClassInfo(javaSupport, cls);


        List<PropInfo> props = new ArrayList<>();
        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (info.isReadable())
            {
                Class<Object> propType = info.getType();
                Class<Object> propTypeHint = info.getTypeHint();


                Method m = find(cls, info.getJavaPropertyName());

                JavaDocs docs = getDocs(m.getDeclaringClass());
                String propDocs = docs.getPropertyDocs().get(info.getJavaPropertyName());


                TypeInfo included = null;
                if (m.getAnnotation(IncludeDocs.class) != null && !known.contains(propType))
                {
                    known.add(propType);
                    included = getTypeInfo(propTypeHint != null ? propTypeHint : propType, known);
                }

                props.add(new PropInfo(info.getJsonName(), className(propType), className(propTypeHint),info.isIgnore(), propDocs, included));

            }

        }

        return new TypeInfo(cls.getName(), getDocs(cls).getClassDoc(), props);
    }


    private Method find(Class<?> cls, String javaPropertyName)
    {
        String name = javaPropertyName.substring(0,1).toUpperCase() + (javaPropertyName.length() > 1 ? javaPropertyName.substring(1) : "");
        String isserName = "is" + name;
        String getterName = "get" + name;

        for (Method m : cls.getMethods())
        {
            String methodName = m.getName();
            if (m.getParameterTypes().length == 0 && (methodName.equals(isserName) || methodName.equals(getterName)))
            {
                return m;
            }
        }

        throw new IllegalStateException("Could not find getter or isser for property '" + javaPropertyName + "' in " + cls);
    }


    private JavaDocs getDocs(Class<?> cls) throws IOException
    {
        if (sourceDir == null)
        {
            throw new UnsupportedOperationException("Only live sourceDir lookup implemened");
        }

        DocsHolder holder = new DocsHolder(cls);
        DocsHolder existing = holders.putIfAbsent(cls, holder);
        if (existing != null && !existing.isModified())
        {
            return existing.getJavaDocs();
        }

        log.debug("Reading for {}", cls);

        return holder.getJavaDocs();
    }



    private String className(Class<Object> typeHint)
    {
        if (typeHint != null)
        {
            return typeHint.getName();
        }
        else
        {
            return null;
        }
    }


    public final static class TypeInfo
    {
        private final String type;
        private final String docs;
        private final List<PropInfo> properties;


        public TypeInfo(String type, String docs, List<PropInfo> properties)
        {
            this.type = type;
            this.docs = docs;
            this.properties = properties;
        }


        public String getType()
        {
            return type;
        }


        public List<PropInfo> getProperties()
        {
            return properties;
        }


        public String getDocs()
        {
            return docs;
        }
    }
    public final static class PropInfo
    {
        private final String name, type, typeHint;
        private final boolean ignored;

        private final String docs;

        private final TypeInfo included;


        public PropInfo(String name, String type, String typeHint, boolean ignored, String docs, TypeInfo included)
        {
            this.name = name;
            this.type = type;
            this.typeHint = typeHint;
            this.ignored = ignored;
            this.docs = docs;
            this.included = included;
        }


        public String getName()
        {
            return name;
        }


        public String getType()
        {
            return type;
        }


        public String getTypeHint()
        {
            return typeHint;
        }


        public boolean isIgnored()
        {
            return ignored;
        }


        public TypeInfo getIncluded()
        {
            return included;
        }


        public String getDocs()
        {
            return docs;
        }
    }

    private class DocsHolder
    {
        private final Class<?> cls;

        private final long created;

        private volatile JavaDocs javaDocs;


        public DocsHolder(Class<?> cls)
        {
            this.cls = cls;
            this.created = System.currentTimeMillis();
        }


        public boolean isModified()
        {
            Class cls = this.cls;
            do
            {
                if (GenerateModelDocs.sourceFile(sourceDir, cls).lastModified() > created)
                {
                    return true;
                }
            } while((cls = cls.getSuperclass()) != null && !cls.equals(Object.class));

            return false;
        }


        public JavaDocs getJavaDocs() throws IOException
        {
            if (javaDocs == null)
            {
                synchronized (this)
                {
                    if (javaDocs == null)
                    {
                        javaDocs = GenerateModelDocs.readJavadocs(sourceDir, cls);
                    }
                }
            }

            return javaDocs;
        }
    }
}
