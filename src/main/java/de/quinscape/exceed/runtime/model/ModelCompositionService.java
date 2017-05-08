package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.ApplicationConfig;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import de.quinscape.exceed.model.DomainEditorViews;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.Layout;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelCompositionService
{
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String CONFIG_MODEL_NAME = "/models/config.json";

    public static final String ROUTING_MODEL_NAME = "/models/routing.json";

    public static final String DOMAIN_MODEL_PREFIX = "/models/domain/";

    public static final String DOMAIN_PROPERTY_MODEL_PREFIX = "/models/domain/property/";

    public static final String ENUM_MODEL_PREFIX = "/models/domain/enum/";

    public static final String DOMAIN_VERSION_PREFIX = "/models/domain/version/";

    public static final String SYSTEM_MODEL_PREFIX = "/models/domain/system/";

    public static final String LAYOUT_MODEL_PREFIX = "/models/layout/";
    public static final String VIEW_MODEL_PREFIX = "/models/view/";

    public static final String PROCESS_MODEL_PREFIX = "/models/process/";

    public static final String PROCESS_VIEW_MODEL_PATTERN = "/models/process/*/view";

    public static final String DOMAIN_LAYOUT_NAME = "/layout/domain.json";

    private final static Logger log = LoggerFactory.getLogger(ModelCompositionService.class);


    private ModelJSONService modelJSONService;

    private final ModelLocationRules modelLocationRules;


    @Autowired
    public ModelCompositionService(ModelLocationRules modelLocationRules, ComponentRegistry componentRegistry)
    {
        this.modelLocationRules = modelLocationRules;
        this.componentRegistry = componentRegistry;
    }


    @Autowired
    public void setModelJSONService(ModelJSONService modelJSONService)
    {
        this.modelJSONService = modelJSONService;
    }

    private final ComponentRegistry componentRegistry;

    public void compose(RuntimeApplication runtimeApplication, Map<String, ResourceLocation> resourceLocations,
                        ApplicationModel applicationModel, DomainService domainService)
    {
        for (ResourceLocation resource : resourceLocations.values())
        {
            if (resource.getRelativePath().endsWith(FileExtension.JSON))
            {
                updateInternal(runtimeApplication, applicationModel, resource.getHighestPriorityResource(), domainService);
            }
        }
    }


    public TopLevelModel update(RuntimeApplication runtimeApplication, ApplicationModel applicationModel, AppResource
        resource, DomainService domainService)
    {
        String path = resource.getRelativePath();

        if (!path.endsWith(FileExtension.JSON))
        {
            throw new IllegalArgumentException(resource + " is not a JSON resource");
        }

        TopLevelModel topLevelModel = updateInternal(runtimeApplication, applicationModel, resource, domainService);

        if (topLevelModel instanceof View)
        {
            postProcessView(runtimeApplication, (View) topLevelModel);
        else if (topLevelModel instanceof LayoutModel)
        {
            LayoutModel layout = (LayoutModel)topLevelModel;

            for (View view : applicationModel.getViews().values())
            {
                final String layoutName = view.getLayout();
                if (layoutName.equals(layout.getName()))
                {
                    final View newView = (View) updateInternal(runtimeApplication, applicationModel, view.getResource(), domainService);
                    postprocessView(runtimeApplication, newView);
                }
            }
        }

        return topLevelModel;
    }


    private TopLevelModel updateInternal(RuntimeApplication runtimeApplication, ApplicationModel applicationModel,
                                         AppResource resource, DomainService domainService)
    {
        String path = resource.getRelativePath();
        String json = new String(resource.read(), UTF8);

        if (json.length() == 0)
        {
            return null;
        }

        try
        {
            final Class<? extends TopLevelModel> type = modelLocationRules.matchType(path);

            if (type == null)
            {
                log.warn("Unknown resource {} at path {}", resource, path);
                return null;
            }

            final TopLevelModel topLevelModel = create(type, json, resource);

            return topLevelModel.accept(new TopLevelModelVisitor<Object, TopLevelModel>()
            {
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
                public TopLevelModel visit(PropertyType propertyType, Object o)
                {
                    applicationModel.addPropertyType(propertyType.getName(), propertyType);
                    return propertyType;
                }


                @Override
                public TopLevelModel visit(Process process, Object o)
                {
                    int nameStart = PROCESS_MODEL_PREFIX.length();
                    String processName = path.substring(nameStart, path.indexOf('/', nameStart));
                    process.setName(processName);
                    applicationModel.addProcess( process.getName(), process);
                    return process;
                }


                @Override
                public TopLevelModel visit(View view, Object o)
                {
                    if (path.startsWith(PROCESS_MODEL_PREFIX))
                    {
                        view.setProcessName(path.substring(PROCESS_MODEL_PREFIX.length(), path.indexOf("/view/")));
                        applicationModel.addView(view.getName(), view);
                        return view;
                    }

                    applicationModel.addView(view.getName(), view);
                    return view;
                }


                @Override
                public TopLevelModel visit(DomainType domainType, Object o)
                {
                    domainType.setDomainService(domainService);

                    if (path.startsWith(SYSTEM_MODEL_PREFIX))
                    {
                        domainType.setSystem(true);

                        if (domainType.getStorageConfiguration() == null)
                        {
                            domainType.setStorageConfiguration(DomainType.SYSTEM_STORAGE);
                        }
                    }

                    applicationModel.addDomainType(domainType.getName(), domainType);
                    return domainType;
                }


                @Override
                public TopLevelModel visit(DomainVersion domainVersion, Object o)
                {
                    applicationModel.addDomainVersion(domainVersion.getName(), domainVersion);
                    return domainVersion;
                }


                @Override
                public TopLevelModel visit(EnumType enumType, Object o)
                {
                    applicationModel.addEnum(enumType.getName(), enumType);
                    return enumType;
                }


                @Override
                public TopLevelModel visit(LayoutModel layoutModel, Object o)
                {
                    applicationModel.addLayout(layoutModel.getName(), layoutModel);
                    return layoutModel;
                }

                @Override
                public TopLevelModel visit(DomainEditorViews domainEditorViews, Object o)
                {
                    applicationModel.getMetaData().setDomainEditorViews(domainEditorViews);
                    return domainEditorViews;
                }

            }, null);
        }
        catch (Exception e)
        {
            throw new ModelLoadingException("Error loading model from " + resource, e);
        }
    }


    public View createViewModel(RuntimeApplication runtimeApplication, AppResource resource, String json, boolean preview)
    {
        View view = create(View.class, json, resource);
        view.setSynthetic(preview);

        return view;
    }

    private final static Pattern PROCESS_PATH = Pattern.compile("^/models/process/(.*)/view/");

    private <M extends Model> M create(Class<M> cls, String json, AppResource resource)
    {

        String path = resource.getRelativePath();

        M model = modelJSONService.toModel(cls, json);


        if (model instanceof TopLevelModel)
        {
            TopLevelModel namedModel = (TopLevelModel) model;
            namedModel.setResource(resource);
            namedModel.setExtension(resource.getResourceRoot().getExtensionIndex());

            String name = nameFromPath(path);
            String processName;
            if (model instanceof View)
            {
                Matcher m = PROCESS_PATH.matcher(path);
                if (m.find())
                {
                    namedModel.setName(Process.getProcessViewName( m.group(1), name));
                }
                else
                {
                    namedModel.setName(name);
                }
            }
            else
            {
                namedModel.setName(name);
            }
        }

        if (model instanceof AutoVersionedModel)
        {
            ((AutoVersionedModel)model).initializeGUIDs();
        }

        return model;
    }

    private String nameFromPath(String path)
    {
        int start = path.lastIndexOf('/');
        int end = path.lastIndexOf('.');
        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = path.length();
        }
        return path.substring(start + 1, end);
    }


    /**
     * Called at the end of the runtime application initialization process.
     *
     * Allows models to be initialized after all models have been read.
     *
     * @param application
     * @param applicationModel
     */
    public void postprocess(RuntimeApplication application, ApplicationModel applicationModel)
    {
        final ScopeMetaModel scopeMetaModel = applicationModel.getMetaData().getScopeMetaModel();

        for (Process process : applicationModel.getProcesses().values())
        {
            process.postProcess(applicationModel);
        }

        validateProcesses(applicationModel);

        for (Process process : applicationModel.getProcesses().values())
        {
            for (ProcessState processState : process.getStates().values())
            {
                if (!(processState instanceof ViewState))
                {
                    scopeMetaModel.addDeclarations(processState);
                    processState.postProcess();
                }
            }
        }

        for (View view : applicationModel.getViews().values())
        {
            postprocessView(application, view);
        }
    }


    private void validateProcesses(ApplicationModel applicationModel)
    {
        for (Process process : applicationModel.getProcesses().values())
        {
            Transition startTransition = process.getStartTransition();
            if (startTransition == null)
            {
                throw new IllegalStateException("Process '" + process.getName() + "' defines no start transition");
            }

            ProcessState processState = process.getStates().get(startTransition.getTo());
            if (processState == null)
            {
                throw new IllegalStateException("Process '" + process.getName() + "' defines a non existing start-state: '" + startTransition + "' does not exist");
            }

            for (ProcessState state : process.getStates().values())
            {
                if (state instanceof ViewState)
                {
                    View view = process.getView(state.getName());
                    if (view == null)
                    {
                        throw new IllegalStateException("Process '" + process.getName() + "' defines a view state '" + state +"' but no such view model exists");
                    }
                }
            }
        }
    }


    public void postProcessView(RuntimeApplication application, View view)
    {
        applyLayout(view, application.getApplicationModel());
        ComponentUtil.updateComponentRegsAndParents(componentRegistry, view, null);
        view.initContext();
        view.setCachedJSON(modelJSONService.toJSON(application, view, JSONFormat.INTERNAL));
    }


    private void applyLayout(View view, ApplicationModel applicationModel)
    {
        final ComponentModel viewComponent = view.getRoot();
        final AttributeValue attr = viewComponent.getAttribute("layout");
        final String layoutName = attr != null ? attr.getValue() : applicationModel.getDefaultLayout();
        final Layout layout = applicationModel.getLayout(layoutName);
        final String layoutFromView = view.getLayout();

        final String effectiveLayout = layoutFromView != null ? layoutFromView : applicationModel.getConfigModel().getDefaultLayout();

        if (effectiveLayout != null)
        {
            final LayoutModel layout = applicationModel.getLayout(effectiveLayout);
            final ContextModel mergedContext = mergeContext(view, layout);
            view.setContextModel(mergedContext);

            // we should only overwrite the "root" content when the current view is no preview. the preview will
            // have a root that is the result of a former merge and which also might be edited.
            final boolean shouldOverwrite = !view.isSynthetic();

            if (shouldOverwrite)
            {
                final ComponentModel layoutRoot = layout.getRoot();
                view.getContent().put(View.ROOT, layoutRoot);
            }
        }
    }


    private ContextModel mergeContext(View view, LayoutModel layout)
    {
        final Map<String, ScopedPropertyModel> mergedProps = new HashMap<>();

        final ContextModel layoutContext = layout.getContextModel();
        if (layoutContext != null)
        {
            mergedProps.putAll(layoutContext.getProperties());
        }
        final ContextModel viewContext = view.getContextModel();
        if (viewContext != null)
        {
            mergedProps.putAll(viewContext.getProperties());
        }

        final ContextModel mergedContext = new ContextModel();
        mergedContext.setProperties(mergedProps);
        return mergedContext;
    }


    public ModelJSONService getModelJSONService()
    {
        return modelJSONService;
    }


    public ModelLocationRules getModelLocationRules()
    {
        return modelLocationRules;
    }
}
