package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.merge.MergeType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;
import org.svenson.info.JavaObjectPropertyInfo;
import org.svenson.util.JSONBeanUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Merges models with different versions of the same type and those type into the application model hierarchy.
 */
public final class ModelMerger
    implements TopLevelModelVisitor<Object, TopLevelModel>
{
    private final static Logger log = LoggerFactory.getLogger(ModelMerger.class);

    private final ApplicationModel applicationModel;

    private final DomainService domainService;

    private final PathResources resources;

    private int newInstanceCount = 0;


    /**
     * Creates a new model merger
     *
     * @param applicationModel      applicationModel to merge into
     * @param domainService         domainService of that application
     * @param resources             path resources at the current location
     */
    ModelMerger(ApplicationModel applicationModel, DomainService domainService, PathResources resources)
    {
        this.applicationModel = applicationModel;
        this.domainService = domainService;
        this.resources = resources;
    }


    @Override
    public TopLevelModel visit(ApplicationConfig configModel, Object o)
    {
        applicationModel.setConfigModel(configModel);
        return configModel;
    }


    @Override
    public TopLevelModel visit(RoutingTable routingTable, Object o)
    {
        applicationModel.setRoutingTable(routingTable);
        return routingTable;
    }


    @Override
    public TopLevelModel visit(PropertyTypeModel propertyType, Object o)
    {
        applicationModel.addPropertyType(propertyType);
        return propertyType;
    }


    @Override
    public TopLevelModel visit(Process process, Object o)
    {
        int nameStart = ModelLocationRules.PROCESS_MODEL_PREFIX.length();

        final String path = resources.getRelativePath();

        String processName = path.substring(nameStart, path.indexOf('/', nameStart));
        process.setName(processName);
        applicationModel.addProcess(process);
        return process;
    }


    @Override
    public TopLevelModel visit(View view, Object o)
    {
        final String path = resources.getRelativePath();
        if (path.startsWith(ModelLocationRules.PROCESS_MODEL_PREFIX))
        {
            view.setProcessName(
                path.substring(ModelLocationRules.PROCESS_MODEL_PREFIX.length(), path.indexOf("/view/")));
            applicationModel.addView(view);
            return view;
        }

        applicationModel.addView(view);
        return view;
    }


    @Override
    public TopLevelModel visit(DomainTypeModel domainType, Object o)
    {
        domainType.setDomainService(domainService);
        applicationModel.addDomainType(domainType);
        return domainType;
    }


    @Override
    public TopLevelModel visit(DomainVersion domainVersion, Object o)
    {
        applicationModel.addDomainVersion(domainVersion);
        return domainVersion;
    }


    @Override
    public TopLevelModel visit(EnumType enumType, Object o)
    {
        applicationModel.addEnum(enumType);
        return enumType;
    }


    @Override
    public TopLevelModel visit(LayoutModel layoutModel, Object o)
    {
        applicationModel.addLayout(layoutModel);
        return layoutModel;
    }


    @Override
    public TopLevelModel visit(DomainRule domainRule, Object in)
    {
        applicationModel.addDomainRule(domainRule);
        return domainRule;
    }


    @Override
    public TopLevelModel visit(QueryTypeModel queryTypeModel)
    {
        queryTypeModel.setDomainService(domainService);
        applicationModel.addDomainType(queryTypeModel);
        return queryTypeModel;
    }


    @Override
    public TopLevelModel visit(StateMachine stateMachine, Object in)
    {
        applicationModel.addStateMachine(stateMachine);
        return stateMachine;
    }


    /**
     * Main entry point for model merging.
     *
     * @param modelJSONService      ModelJSONService impl
     * @param applicationModel      application model to insert
     * @param domainService         domain service of that application
     * @param type                  Model type of the update
     * @param changedRoot           Resource root in which the update occured or <code>null</code>
     * @param resources             new or updated path resources
     * @return
     */

    public static TopLevelModel merge(
        ModelJSONService modelJSONService,
        ApplicationModel applicationModel,
        DomainService domainService,
        Class<? extends TopLevelModel> type,
        FileResourceRoot changedRoot,
        PathResources resources
    )
    {
        final ModelMerger merger = new ModelMerger(applicationModel, domainService, resources);

        MergeStrategy mergeStrategy = type.getAnnotation(MergeStrategy.class);

        final MergeType mergeType;
        if (mergeStrategy != null)
        {
            mergeType = mergeStrategy.value();
        }
        else
        {
            mergeType = MergeType.REPLACE;
        }

        final TopLevelModel model;
        switch (mergeType)
        {
            case REPLACE:
                final AppResource topResource = resources.getHighestPriorityResource();
                final boolean highestChanged = changedRoot == null || topResource.getResourceRoot().equals(changedRoot);
                if (!highestChanged)
                {
                    return null;
                }
                String json = new String(topResource.read(), RequestUtil.UTF_8);

                model = ModelCompositionService.create(
                    modelJSONService, type, json, topResource
                );

                break;

            case DEEP:

                model = (TopLevelModel) resources.getAppResources()
                    .stream()
                    .map(r ->
                        (Object) ModelCompositionService.create(
                            modelJSONService,
                            type,
                            new String(r.read(), RequestUtil.UTF_8),
                            r
                        )
                    )
                    .reduce(null, merger::merge);
                break;
            default:
                throw new IllegalStateException("Unhandled mergeType: " + mergeType);
        }

        model.accept(merger, null);
        return model;
    }


    /**
     * Reducer function merging to objects into one.
     *
     * @param curr      current object
     * @param next      next object
     *
     * @param <T> type of the objects
     *
     * @return merged object
     */
    <T> T merge(T curr, T next)
    {
        log.debug("MERGE {}, {}", curr, next);

        if (curr == null || isForcedReplacement(curr.getClass()))
        {
            return next;
        }
        else if (next == null)
        {
            return curr;
        }
        else
        {
            T merged = newInstance(curr);

            if (curr instanceof Set)
            {
                ((Set)merged).addAll((Set) curr);
                ((Set)merged).addAll((Set) next);
            }
            else if (curr instanceof List)
            {
                ((List)merged).addAll((Collection) curr);
                ((List)merged).addAll((Collection) next);
            }
            else if (curr instanceof Map)
            {
                merged = (T) mergeMap((Map<String,Object>) curr, (Map<String,Object>) next);
            }
            else
            {
                final JSONClassInfo classInfo = JSONUtil.getClassInfo(curr.getClass());

                final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;


                if (log.isDebugEnabled())
                {
                    log.debug(
                        "PROPS: {}", classInfo.getPropertyInfos().stream().map(JSONPropertyInfo::getJsonName).collect(
                            Collectors.toList()));
                }

                for (JSONPropertyInfo info : classInfo.getPropertyInfos())
                {
                    if (info.isReadOnly() || !info.isWriteable() || info.isIgnore())
                    {
                        continue;
                    }

                    JavaObjectPropertyInfo javaInfo = (JavaObjectPropertyInfo) info;

                    final Class<Object> propertyType = info.getType();

                    final boolean isMap = Map.class.isAssignableFrom(propertyType);
                    final boolean isCollection = Collection.class.isAssignableFrom(propertyType);
                    final String propertyName = info.getJsonName();

                    if (isForcedReplacement(propertyType))
                    {
                        final Object currValue = util.getProperty(curr, propertyName);
                        final Object nextValue = util.getProperty(next, propertyName);
                        if (nextValue != null)
                        {
                            util.setProperty(merged, propertyName, nextValue);
                        }
                        else if (currValue != null)
                        {
                            util.setProperty(merged, propertyName, currValue);
                        }
                    }
                    else
                    {
                        MergeType newMergeType = MergeType.REPLACE;
                        if (isMap || isCollection)
                        {
                            final Class<Object> typeHint = info.getTypeHint();

                            if (typeHint != null)
                            {
                                final MergeStrategy anno = typeHint.getAnnotation(MergeStrategy.class);
                                if (anno != null)
                                {
                                    newMergeType = anno.value();
                                }
                            }

                            final MergeStrategy propAnno = JSONUtil.findAnnotation(info, MergeStrategy.class);
                            if (propAnno != null)
                            {
                                newMergeType = propAnno.value();
                            }

                            log.debug("property {}: {}", propertyName, newMergeType);


                            if (isMap)
                            {
                                final Map<String, Object> currMap = (Map<String, Object>) util.getProperty(
                                    curr, propertyName);
                                final Map<String, Object> result;
                                if (newMergeType == MergeType.REPLACE)
                                {
                                    log.debug("replace items in map");

                                    result = newInstance(currMap);
                                    result.putAll(currMap);
                                    result.putAll((Map<String, Object>) util.getProperty(next, propertyName));

                                }
                                else
                                {
                                    log.debug("merge items in map");

                                    final Map<String, Object> nextMap = (Map<String, Object>) util.getProperty(
                                        next, propertyName);

                                    result = mergeMap(currMap, nextMap);
                                }
                                util.setProperty(merged, propertyName, result);
                            }
                            else
                            {
                                final List<Object> result;
                                if (newMergeType == MergeType.REPLACE)
                                {
                                    log.debug("replace list");

                                    result = (List<Object>) util.getProperty(next, propertyName);
                                }
                                else
                                {
                                    log.debug("add to list");

                                    final List<Object> currList = (List<Object>) util.getProperty(curr, propertyName);

                                    result = new ArrayList<>();
                                    if (currList != null)
                                    {
                                        result.add(currList);
                                    }
                                    final List<Object> nextList = (List<Object>) util.getProperty(
                                        next, propertyName);
                                    if (nextList != null)
                                    {
                                        result.addAll(nextList);
                                    }

                                }
                                util.setProperty(merged, propertyName, result);
                            }
                        }
                        else
                        {
                            final Object result = merge(
                                util.getProperty(curr, propertyName),
                                util.getProperty(next, propertyName)
                            );
                            util.setProperty(merged, propertyName, result);
                        }
                    }
                }

            }
            return merged;
        }
    }


    private Map<String, Object> mergeMap(Map<String, Object> currMap, Map<String, Object> nextMap)
    {
        Map<String, Object> result;
        result = nextMap != null ? newInstance(nextMap) : new HashMap<>();

        for (Map.Entry<String, Object> e : nextMap.entrySet())
        {
            final Object value = currMap.get(e.getKey());
            if (value != null)
            {
                result.put(e.getKey(), merge(e.getValue(), value));
            }
            else
            {
                result.put(e.getKey(), value);
            }

        }
        return result;
    }


    private boolean isForcedReplacement(Class<?> cls)
    {
        return cls.isPrimitive() ||
            String.class.isAssignableFrom(cls) ||
            Boolean.class.isAssignableFrom(cls) ||
            Enum.class.isAssignableFrom(cls) ||
            AppResource.class.isAssignableFrom(cls) ||
            Number.class.isAssignableFrom(cls);
    }


    private <T> T newInstance(Object curr)
    {
        if (curr == null)
        {
            throw new IllegalArgumentException("curr can't be null");
        }


        newInstanceCount++;

        if (curr instanceof Set)
        {
            return (T) new HashSet();
        }
        else if (curr instanceof List)
        {
            return (T) new ArrayList();
        }
        else if (curr instanceof TreeMap)
        {
            return (T) new TreeMap();
        }
        else if (curr instanceof Map)
        {
            return (T) new HashMap<>();
        }
        else
        {
            try
            {
                return (T) curr.getClass().newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new ExceedRuntimeException(e);
            }
        }
    }


    public int getNewInstanceCount()
    {
        return newInstanceCount;
    }
}
