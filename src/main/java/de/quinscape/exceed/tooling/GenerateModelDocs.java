package de.quinscape.exceed.tooling;

import com.github.javaparser.ParseException;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.DocumentedMapKey;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.annotation.DocumentedSubTypes;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.component.ComponentPackageDescriptor;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.config.ModelConfiguration;
import de.quinscape.exceed.runtime.model.ModelLocationRule;
import de.quinscape.exceed.runtime.model.ModelLocationRules;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * Autog-generates {@link ModelDocs} from the current model classes annotated with a few extra documentation annotations.
 *
 * @see DocumentedMapKey
 * @see DocumentedModelType
 * @see DocumentedSubTypes
 * @see Internal
 *
 * @see de.quinscape.exceed.runtime.service.client.provider.docs.ModelDocsProvider
 *
 */
public class GenerateModelDocs
{
    private final static Logger log = LoggerFactory.getLogger(GenerateModelDocs.class);

    private final static String DEFAULT_KEY_NAME = "key";
    private  final static Map<Class<?>, String> PROPERTY_TYPE_NAMES;
    static
    {
        final HashMap<Class<?>, String> map = new HashMap<>();

        map.put(Boolean.class, "boolean");
        map.put(Boolean.TYPE, "boolean");
        map.put(Byte.class, "int");
        map.put(Byte.TYPE, "int");
        map.put(Short.class, "int");
        map.put(Short.TYPE, "int");
        map.put(Integer.class, "int");
        map.put(Integer.TYPE, "int");
        map.put(Long.class, "long");
        map.put(Long.TYPE, "long");
        map.put(String.class, "String");
        map.put(Timestamp.class, "Timestamp");
        map.put(Date.class, "Date");
        map.put(Object.class, "Object");
        map.put(ASTExpression.class, "Expression");
        map.put(AttributeValueType.class, "AttributeValueType");

        PROPERTY_TYPE_NAMES = Collections.unmodifiableMap(map);
    }


    private ModelLocationRules modelLocationRules;

    /**
     * Maps model classes to {@link JavaDocs} instances
     */
    private Map<Class<?>, JavaDocs> docsMap = new HashMap<>();

    public ModelDocs getModelDocs()
    {
        try
        {

            Set<Class<? extends TopLevelModel>> topLevelModels = readJavadocs();

            // instantiate the spring model configuration just for the modelLocationRules configured in it.
            modelLocationRules = new ModelConfiguration().modelLocationRules();

            List<String> topLevelModelDocs = new ArrayList<>();

            final HashMap<String, ModelDoc> modelDocs = new HashMap<>();
            for (Class<? extends TopLevelModel> cls : topLevelModels)
            {
                final ModelDoc doc = createModelDoc(cls, modelDocs);
                if (doc != null)
                {
                    topLevelModelDocs.add(doc.getType());
                }
            }
            return new ModelDocs(topLevelModelDocs, modelDocs);
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    public Set<Class<? extends TopLevelModel>> readJavadocs() throws ClassNotFoundException, IOException
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(Object.class));
        Set<BeanDefinition> candidates = provider.findCandidateComponents(Model.class.getPackage().getName());

        Set<Class<? extends TopLevelModel>> topLevelModels = new TreeSet<>(new ClassComparator());

        for (BeanDefinition definition : candidates)
        {
            Class<?> cls = Class.forName(definition.getBeanClassName());
            do
            {
                if (TopLevelModel.class.isAssignableFrom(cls) && cls.getAnnotation(Internal.class) == null)
                {
                    topLevelModels.add((Class<? extends TopLevelModel>) cls);
                }
                collectJavadocs(cls);

            } while ((cls = cls.getSuperclass()) != null && Model.class.isAssignableFrom(cls));
        }


        topLevelModels.remove(TopLevelModel.class);
        return topLevelModels;
    }


    private ModelDoc createModelDoc(Class<?> cls, HashMap<String, ModelDoc> visited)
    {
        try
        {
            final String modelType = Model.getType(cls);
            final ModelDoc existing = visited.get(modelType);
            if (existing != null)
            {
                return existing;
            }
            final ModelDoc modelDoc = new ModelDoc(modelType);
            visited.put(modelType, modelDoc);

            log.info("Analyze {}", cls);

            final JSONClassInfo classInfo = JSONUtil.getClassInfo(cls);

            String classDescription = null;
            String locationDescription;
            final JavaDocs javaDocs = docsMap.get(cls);
            if (javaDocs != null)
            {
                classDescription = javaDocs.getClassDoc();
            }

            if (!cls.equals(ComponentPackageDescriptor.class))
            {
                locationDescription = describeLocations((Class<? extends TopLevelModel>) cls);
            }
            else
            {
                locationDescription = "components.json declarations";
            }

            List<ModelPropertyDoc> propertyDocs = new ArrayList<>();

            for (JSONPropertyInfo info : classInfo.getPropertyInfos())
            {
                if (info == null || info.isIgnore() || (JSONUtil.findAnnotation(cls, info, Internal.class) != null))
                {
                    continue;
                }

                final Class<Object> type = info.getType();
                final Class<Object> typeHint = info.getTypeHint();


                final String propName = info.getJsonName();

                String propertyDescription = cleanupDoc(findPropertyDoc(cls, (JavaObjectPropertyInfo) info));

                final DocumentedMapKey keyAnno = JSONUtil.findAnnotation(cls, info, DocumentedMapKey.class);
                final DocumentedModelType typeAnno = JSONUtil.findAnnotation(cls, info, DocumentedModelType.class);
                final DocumentedSubTypes subTypesAnno = JSONUtil.findAnnotation(cls, info, DocumentedSubTypes.class);
                String keyName = keyAnno != null ? keyAnno.value() : DEFAULT_KEY_NAME;


                String propTypeDescription;

                List<String> subTypeDocs = null;
                if (Collection.class.isAssignableFrom(type))
                {
                    final String desc = getTypeDescription(typeHint);
                    if (desc != null )
                    {
                        propTypeDescription = typeAnno != null ? typeAnno.value() : "Array of " + desc;
                    }
                    else
                    {
                        if (typeHint == null)
                        {
                            propTypeDescription = typeAnno != null ? typeAnno.value() : "Array";
                        }
                        else
                        {
                            propTypeDescription = typeAnno != null ? typeAnno.value() : "Array of " + typeHint.getSimpleName();
                            subTypeDocs = Collections.singletonList(createModelDoc(typeHint, visited).getType());
                        }
                    }
                }
                else if (Map.class.isAssignableFrom(type))
                {
                    final String desc = getTypeDescription(typeHint);
                    if (desc != null )
                    {
                        propTypeDescription = typeAnno != null ? typeAnno.value() : "Map " + keyName + " ->  " + desc;
                    }
                    else
                    {
                        if (typeHint == null)
                        {
                            propTypeDescription = typeAnno != null ? typeAnno.value() : "Map " + keyName + " ->  Object";
                        }
                        else
                        {
                            propTypeDescription = typeAnno != null ? typeAnno.value() : "Map " + keyName + " -> " + typeHint.getSimpleName();
                            subTypeDocs = Collections.singletonList(createModelDoc(typeHint, visited).getType());
                        }
                    }
                }
                else if (Enum.class.isAssignableFrom(type))
                {
                    propTypeDescription = type.getSimpleName() + " Enum";
                    propertyDescription += "<br/>Values: " + getEnumValues(type);
                }
                else
                {
                    propTypeDescription = typeAnno != null ? typeAnno.value() : getTypeDescription(type);
                    if (propTypeDescription == null)
                    {
                        propTypeDescription = type.getSimpleName();
                        subTypeDocs = Collections.singletonList(createModelDoc(type, visited).getType());
                    }
                }

                if (subTypesAnno != null)
                {
                    subTypeDocs = Arrays.stream(subTypesAnno.value())
                        .map(subTypeCls ->
                        {
                            return createModelDoc(subTypeCls, visited).getType();
                        })
                        .collect(Collectors.toList());
                }

                propertyDocs.add(new ModelPropertyDoc(propName, propTypeDescription, propertyDescription, subTypeDocs));
            }

            modelDoc.setClassDescription(classDescription);
            modelDoc.setLocationDescription(locationDescription);
            modelDoc.setPropertyDocs(propertyDocs);
            return modelDoc;
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private String cleanupDoc(String propertyDoc)
    {
        if (propertyDoc == null)
        {
            return null;
        }

        return propertyDoc
            .replaceAll("@return.*?(\n|$)", "")
            .replaceAll("\\{@link (.*?)}", "$1");
    }


    private String getEnumValues(Class<Object> type) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        Enum[] enums = (Enum[]) type.getMethod("values").invoke(null);

        for (int i = 0; i < enums.length; i++)
        {
            Enum value = enums[i];
            if (i > 0)
            {
                sb.append(", ");
            }
            sb.append(value.name());
        }

        return sb.toString();
    }


    private String findPropertyDoc(Class<?> cls, JavaObjectPropertyInfo info)
    {
        if (info.isReadable())
        {
            final JavaDocs javaDocs = docsMap.get(info.getGetterMethod().getDeclaringClass());
            if (javaDocs != null)
            {
                final String propDoc = javaDocs.getPropertyDocs().get(info.getJavaPropertyName());
                if (propDoc != null)
                {
                    return propDoc;
                }
            }

        }
        if (info.isWriteable())
        {
            final JavaDocs javaDocs = docsMap.get(info.getSetterMethod().getDeclaringClass());
            if (javaDocs != null)
            {
                final String propDoc = javaDocs.getPropertyDocs().get(info.getJavaPropertyName());
                if (propDoc != null)
                {
                    return propDoc;
                }
            }

        }
        return null;
    }


    private String getTypeDescription(Class<Object> typeHint)
    {
        if (typeHint == null)
        {
            return null;
        }

        final String name = PROPERTY_TYPE_NAMES.get(typeHint);
        if (name != null)
        {
            return name;
        }

        return null;
    }




    private void indent(StringBuilder sb, int depth)
    {
        for (int i=0; i < depth; i++)
        {
            sb.append("    ");
        }
    }

    private String describeLocations(Class<? extends TopLevelModel> cls)
    {
        boolean first = true;

        StringBuilder sb = new StringBuilder();
        for (ModelLocationRule rule : modelLocationRules.getRules())
        {
            if (rule.getType().equals(Model.getType(cls)))
            {
                if (!first)
                {
                    sb.append(" or ");
                }
                first = false;

                final String prefix = rule.getPrefix();
                final String suffix = rule.getSuffix();

                if (suffix == null)
                {
                    sb.append(prefix);
                }
                else
                {
                    sb.append(prefix).append('*').append(suffix);
                }
            }
        }

        return sb.toString();
    }


    private JavaDocs collectJavadocs(Class<?> cls) throws IOException
    {
        JavaDocs javaDocs = docsMap.get(cls);
        if (javaDocs == null)
        {
            if (cls.equals(ScopedPropertyModel.class))
            {
                log.debug("hit");
            }

            File sourceDir = Util.getExceedLibrarySource();

            javaDocs = readJavadocs(new File(sourceDir, "./src/main/java/".replace('/', File.separatorChar)),cls);
            docsMap.put(cls, javaDocs);
        }

        return javaDocs;
    }


    public static JavaDocs readJavadocs(File base, Class<?> declaringClass) throws IOException
    {
        File source =  JavaSourceUtil.sourceFile(base, declaringClass);
        if (!source.exists())
        {
            throw new RuntimeException("Source " + source + " does not exist: pwd is " + new File(".").getAbsolutePath());
        }

        try
        {
            return new JavaDocs(source);
        }
        catch (ParseException | IllegalAccessException | InstantiationException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private class ClassComparator
        implements Comparator<Class<?>>
    {
        @Override
        public int compare(Class<?> o1, Class<?> o2)
        {
            return getName(o1).compareTo(getName(o2));
        }


        private String getName(Class<?> o2)
        {
            // sort the component package descriptor last
            if (o2.equals(ComponentPackageDescriptor.class))
            {
                return "zzz";
            }

            return o2.getSimpleName();
        }
    }
}
