package de.quinscape.exceed.tooling;

import com.github.javaparser.ParseException;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.AbstractModel;
import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.DocumentedMapKey;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.annotation.DocumentedSubTypes;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.component.ComponentPackageDescriptor;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.domain.QueryTypeOperations;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import de.quinscape.exceed.runtime.expression.OperationKey;
import de.quinscape.exceed.runtime.expression.OperationRegistration;
import de.quinscape.exceed.runtime.expression.annotation.OperationParam;
import de.quinscape.exceed.runtime.expression.query.FieldOperations;
import de.quinscape.exceed.runtime.expression.query.QueryFilterOperations;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.DefinitionsBuilder;
import de.quinscape.exceed.runtime.model.ModelLocationRule;
import de.quinscape.exceed.runtime.model.ModelLocationRules;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;
import org.svenson.util.JSONBuilder;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * Auto-generates {@link ModelDocs} from the current model classes annotated with a few extra documentation annotations.
 *
 * @see DocumentedMapKey
 * @see DocumentedModelType
 * @see DocumentedSubTypes
 * @see Internal
 *
 *
 */
public class GenerateModelDocs
{
    private final static Logger log = LoggerFactory.getLogger(GenerateModelDocs.class);

    private final static String DEFAULT_KEY_NAME = "key";
    private  final static Map<Class<?>, String> PROPERTY_TYPE_NAMES;

    private static final String VOID = "void";


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
        map.put(ExpressionValueType.class, "AttributeValueType");

        PROPERTY_TYPE_NAMES = Collections.unmodifiableMap(map);
    }


    /**
     * Maps model classes to {@link JavaDocs} instances
     */
    private Map<Class<?>, JavaDocs> docsMap = new HashMap<>();

    private ModelLocationRules modelLocationRules = new ModelLocationRules();


    public ModelDocs getModelDocs()
    {
        try
        {

            Set<Class<? extends TopLevelModel>> topLevelModels = readJavadocs();

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
        Set<BeanDefinition> candidates = provider.findCandidateComponents(AbstractModel.class.getPackage().getName());

        Set<Class<? extends TopLevelModel>> topLevelModels = new TreeSet<>(new ClassComparator());

        for (BeanDefinition definition : candidates)
        {
            Class<?> cls = Class.forName(definition.getBeanClassName());
            do
            {
                if (AbstractTopLevelModel.class.isAssignableFrom(cls) && cls.getAnnotation(Internal.class) == null)
                {
                    topLevelModels.add((Class<? extends TopLevelModel>) cls);
                }
                collectJavadocs(cls);

            } while ((cls = cls.getSuperclass()) != null && AbstractModel.class.isAssignableFrom(cls));
        }


        topLevelModels.remove(AbstractTopLevelModel.class);
        return topLevelModels;
    }


    private ModelDoc createModelDoc(Class<?> cls, HashMap<String, ModelDoc> visited)
    {
        try
        {
            final String modelType = Model.findType(cls);
            if (modelType == null)
            {
                return null;
            }
            
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
                final String propName = info.getJsonName();

                if (info == null || info.isIgnore() || (JSONUtil.findAnnotation(info, Internal.class) != null) || propName.equals(
                    AutoVersionedModel.IDENTITY_GUID) || propName.equals(AutoVersionedModel.VERSION_GUID) )
                {
                    continue;
                }

                final Class<Object> type = info.getType();
                final Class<Object> typeHint = info.getTypeHint();



                String propertyDescription = cleanupDoc(findPropertyDoc(cls, (JavaObjectPropertyInfo) info));

                final DocumentedMapKey keyAnno = JSONUtil.findAnnotation(info, DocumentedMapKey.class);
                final DocumentedModelType typeAnno = JSONUtil.findAnnotation(info, DocumentedModelType.class);
                final DocumentedSubTypes subTypesAnno = JSONUtil.findAnnotation(info, DocumentedSubTypes.class);
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
                            subTypeDocs = toSingletonTypeList(createModelDoc(typeHint, visited));
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
                            subTypeDocs = toSingletonTypeList(createModelDoc(typeHint, visited));
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
                        subTypeDocs = toSingletonTypeList(createModelDoc(type, visited));
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


    private List<String> toSingletonTypeList(ModelDoc doc)
    {
        if (doc == null)
        {
            return null;
        }
        else
        {
            return Collections.singletonList(doc.getType());
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
        for (ModelLocationRule rule : getModelLocationRules().getRules())
        {
            if (rule.getType().equals(Model.findType(cls)))
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


            javaDocs = readJavadocs(getSourceBaseDir(),cls);
            docsMap.put(cls, javaDocs);
        }

        return javaDocs;
    }


    private File getSourceBaseDir()
    {
        File sourceDir = Util.getExceedLibrarySource();
        return new File(sourceDir, "./src/main/java/".replace('/', File.separatorChar));
    }


    public ModelLocationRules getModelLocationRules()
    {
        return modelLocationRules;
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



    private ConfigurableApplicationContext getExpressionContext()
    {
        return SpringApplication.run(ExpressionDocBaseConfiguration.class);
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


    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: GenerateModelDocs <json-file-path>");
            System.exit(1);
        }

        new GenerateModelDocs().main(args[0]);

    }


    private void main(String target) throws IOException
    {
        final ConfigurableApplicationContext ctx = getExpressionContext();

        final List<ModelLocationRule> rules = getModelLocationRules().getRules()
            .stream()
            .filter(
                rule ->
                    Model.getType(rule.getType()).getAnnotation(Internal.class) == null
            )
            .collect(Collectors.toList());
        
        final JSONBuilder builder = JSONBuilder.buildObject()
            .property("locations", rules)
            .property("modelDocs", getModelDocs())
            .property("definitions", ctx.getBean(Definitions.class));

        addQueryExprDefinitions(ctx, builder);

        final String json = builder.output();

        FileUtils.writeStringToFile(
            new File(target),
            json,
            "UTF-8"
        );
    }


    private void addQueryExprDefinitions(ConfigurableApplicationContext ctx, JSONBuilder jsonBuilder) throws IOException
    {
        final ExpressionServiceImpl service = (ExpressionServiceImpl)ctx.getBean(ExpressionService.class);


        final Map<OperationKey, OperationRegistration> ops = service.getOperationLookup();

        Set<Class<?>> opClasses = findOperationBeans(ops.values());

        final Map<Class<?>, JavaDocs> javaDocs = getJavaDocs(opClasses);


        for (Class<?> cls : opClasses)
        {
            final boolean isQueryTypeOperations = cls.equals(QueryTypeOperations.class);
            final boolean isQueryFilterOperations = cls.equals(QueryFilterOperations.class);

            final DefinitionsBuilder builder = Definition.builder();
            for (Map.Entry<OperationKey, OperationRegistration> e : ops.entrySet())
            {
                final OperationKey key = e.getKey();
                final OperationRegistration registration = e.getValue();

                final Class<?> cur = registration.getBean().getClass();
                if (cur.equals(cls))
                {
                    final JavaDocs javaDoc = javaDocs.get(cur);

                    final Class<?> context = key.getContext();
                    final String contextName = context == null ? null : context.getSimpleName();

                    final String returnType = registration.getReturnType().getSimpleName();
                    final String operationName = key.getName();
                    builder.function(contextName != null ? contextName +  "." + operationName : operationName)
                        .withType(isQueryTypeOperations ? DefinitionType.QUERY : DefinitionType.QUERY_FILTER)
                        .chapter(contextName)
                        .asOperationFor(context)
                        .withDescription(javaDoc.getPropertyDocs().get(registration.getTypeDescription()))
                        .withParameterModels(
                            getQueryPropModels(
                                registration.getParameterTypes(),
                                context == null ? 1 : 2,
                                registration.getOperationParams()
                            )
                        )
                        .withReturnType(returnType.equals(VOID) ? null : returnType)
                        .build();
                }
            }

            if (isQueryFilterOperations)
            {
                final String contextName = "Field";

                for (String operationName : FieldOperations.getOperationNames())
                {
                    builder.function(contextName != null ? contextName +  "." + operationName : operationName)
                        .withType(isQueryTypeOperations ? DefinitionType.QUERY : DefinitionType.QUERY_FILTER)
                        .chapter(contextName)
                        .asOperationFor(Field.class)
                        .withDescription("JOOQ Field method " + operationName)
                        .withParameterModels(
                            getQueryPropModels(
                                FieldOperations.getParameterTypes(operationName),
                                0,
                                null
                            )
                        )
                        .withReturnType("Condition")
                        .build();

                }

            }

            jsonBuilder.property(cls.getSimpleName(), builder.build());
        }
    }


    private Set<Class<?>> findOperationBeans(Collection<OperationRegistration> values)
    {
        Set<Class<?>> set = new HashSet<>();

        for (OperationRegistration registration : values)
       {
            final Class<?> cls = registration.getBean().getClass();
            if (!set.contains(cls))
            {
                set.add(cls);
            }
        }
        return set;
    }


    /**
     * Creates a list of documentation property models for the given parameter types.
     *
     * @param parameterTypes    parameter types;
     * @param start             numbers of parameters to ignore at the beginning
     * @param operationParams   @OperationParam annotations
     *
     * @return List of documentation property models
     */
    private List<DomainProperty> getQueryPropModels(
        Class<?>[] parameterTypes, int start,
        OperationParam[] operationParams
    )
    {

        final List<DomainProperty> list = new ArrayList<>();

        if (operationParams != null && operationParams.length > 0)
        {
            for (OperationParam param : operationParams)
            {
                list.add(
                    DomainProperty.builder()
                        .withName(param.name())
                        .withType(param.type())
                        .withDescription(param.description())
                        .build()
                );
            }
        }
        else
        {
            for (int i = start; i < parameterTypes.length; i++)
            {
                Class<?> cls = parameterTypes[i];
                list.add(
                    DomainProperty.builder().withType(cls.getSimpleName()).build()
                );
            }
        }

        return list;
    }


    private Map<Class<?>, JavaDocs> getJavaDocs(Set<Class<?>> values) throws IOException
    {
        Map<Class<?>, JavaDocs> javaDocs = new HashMap<>();

        File sourceDir = getSourceBaseDir();
        for (Class<?> cls : values)
        {
            if (javaDocs.get(cls) == null)
            {
                javaDocs.put(cls, readJavadocs( sourceDir, cls));
            }
        }
        return javaDocs;
    }
}
