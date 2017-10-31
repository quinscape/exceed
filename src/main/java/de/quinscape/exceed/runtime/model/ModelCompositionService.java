package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.meta.ApplicationMetaData;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.js.JsEnvironmentFactory;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;

public class ModelCompositionService
{
    private static final Charset UTF8 = Charset.forName("UTF-8");


    private final static Logger log = LoggerFactory.getLogger(ModelCompositionService.class);

    private final ModelJSONService modelJSONService;
    private final ModelLocationRules modelLocationRules;
    private final ModelSchemaService modelSchemaService;
    private final JsEnvironmentFactory jsEnvironmentFactory;
    private final ComponentRegistry componentRegistry;

    @Autowired
    public ModelCompositionService(ModelLocationRules modelLocationRules, ModelSchemaService modelSchemaService, ComponentRegistry componentRegistry, ModelJSONService modelJSONService, JsEnvironmentFactory jsEnvironmentFactory)
    {
        this.modelLocationRules = modelLocationRules;
        this.modelSchemaService = modelSchemaService;
        this.componentRegistry = componentRegistry;
        this.modelJSONService = modelJSONService;
        this.jsEnvironmentFactory = jsEnvironmentFactory;
    }


    /**
     * Creates a fully assembled application model from the resources provided by the given resource loader.
     * <p>
     *  The JSON resources provided by the resource loader are all loaded and inserted into the application object
     *  graph. 
     * </p>
     *
     * @param resourceLocations        resource loader
     * @param domainService         domain service of the application
     * @param systemDefinitions     Base system definitions
     * @param appName               application name
     *
     * @return assembled application model.
     */
    public ApplicationModel compose(
        Collection<PathResources> resourceLocations,
        DomainService domainService,
        Definitions systemDefinitions,
        String appName
    )
    {
        ApplicationModel applicationModel = new ApplicationModel(systemDefinitions);
        applicationModel.setName(appName);
        applicationModel.setVersion(UUID.randomUUID().toString());

        for (PathResources resource : resourceLocations)
        {
            if (resource.getRelativePath().endsWith(FileExtension.JSON))
            {
                updateInternal(applicationModel, domainService, resource.getHighestPriorityResource());
            }
        }

        return applicationModel;
    }


    /**
     * Updates the model corresponding to the given resource.
     *
     * @param applicationModel      application model
     * @param domainService         domain service
     * @param resource              resource that has changed content
     *
     * @return top level model that has been created for the resource contents.
     */
    public TopLevelModel update(
        ApplicationModel applicationModel,
        DomainService domainService,
        AppResource resource
    )
    {
        String path = resource.getRelativePath();

        if (!path.endsWith(FileExtension.JSON))
        {
            throw new IllegalArgumentException(resource + " is not a JSON resource");
        }

        TopLevelModel topLevelModel = updateInternal(applicationModel, domainService, resource);

        applicationModel.setVersion(UUID.randomUUID().toString());

        final ApplicationMetaData metaData = applicationModel.getMetaData();
        final ScopeMetaModel scopeMetaModel = metaData.getScopeMetaModel();

        if (topLevelModel instanceof View)
        {
            final View newView = (View) topLevelModel;

            postprocessView(applicationModel, newView);

            metaData.postProcess();
            metaData.validate();


//            final String processName = newView.getProcessName();
//            if (processName == null)
//            {
//                scopeMetaModel.addDeclarations(newView);
//            }
//            else
//            {
//                final Process process = applicationModel.getProcess(processName);
//                final ViewState viewState = (ViewState)process.getStates().get(newView.getLocalName());
//                scopeMetaModel.addDeclarations(viewState);
//            }
        }
        else if (topLevelModel instanceof Process)
        {
            final Process newProcess = (Process) topLevelModel;
            newProcess.postProcess(applicationModel);

            for (ProcessState processState : newProcess.getStates().values())
            {
                scopeMetaModel.addDeclarations(processState);
                processState.postProcess();
            }
        }
        else if (topLevelModel instanceof LayoutModel)
        {
            LayoutModel layout = (LayoutModel)topLevelModel;

            for (View view : applicationModel.getViews().values())
            {
                final String layoutName = view.getLayout(applicationModel);
                if (layoutName.equals(layout.getName()))
                {
                    final View newView = (View) updateInternal(applicationModel, domainService, view.getResource());
                    postprocessView(applicationModel, newView);
                    scopeMetaModel.addDeclarations(newView);
                }
            }
        }
        else if (topLevelModel instanceof ApplicationConfig)
        {
            ApplicationConfig newConfig = (ApplicationConfig)topLevelModel;
            applicationModel.setConfigModel(newConfig);

            // redo full meta postprocessing if config changes
            // recycle the js environment which will detect changes and update the js contexts contained automatically
            metaData.postProcess();
        }
        else if (topLevelModel instanceof QueryTypeModel)
        {
            // XXX: hack
            ((DomainServiceImpl)domainService).update((QueryTypeModel) topLevelModel);
        }

        return topLevelModel;
    }


    private TopLevelModel updateInternal(
        ApplicationModel applicationModel,
        DomainService domainService,
        AppResource resource
    )
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
                log.debug("Unknown resource {} at path {}", resource, path);
                return null;
            }

            final TopLevelModel topLevelModel = create(type, json, resource);

            return topLevelModel.accept(new TopLevelModelMerger(applicationModel, path, domainService), null);
        }
        catch (Exception e)
        {
            throw new ModelLoadingException("Error loading model from " + resource, e);
        }
    }


    public View createPreviewViewModel(AppResource resource, String json)
    {
        View view = create(View.class, json, resource);
        view.setSynthetic(true);

        return view;
    }

    private final static Pattern PROCESS_PATH = Pattern.compile("^/models/process/(.*?)/view/");

    private <M extends TopLevelModel> M create(Class<M> cls, String json, AppResource resource)
    {
        final String path = resource.getRelativePath();

        final M model = modelJSONService.toModel(cls, json);

        model.setResource(resource);
        model.setExtension(resource.getResourceRoot().getExtensionIndex());

        final String name = nameFromPath(path);
        if (model instanceof View)
        {
            Matcher m = PROCESS_PATH.matcher(path);
            if (m.find())
            {
                model.setName(Process.getProcessStateName( m.group(1), name));
            }
            else
            {
                model.setName(name);
            }
        }
        else
        {
            model.setName(name);
        }

        model.initializeGUIDs();

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
     * @param applicationModel       application model
     */
    public void postprocess(ApplicationModel applicationModel)
    {

        for (DomainType domainType : modelSchemaService.getModelDomainTypes().values())
        {
            applicationModel.addDomainType(domainType);
        }

        for (EnumType enumType : modelSchemaService.getModelEnumTypes().values())
        {
            applicationModel.addEnum(enumType);
        }


        for (DomainType domainType : applicationModel.getDomainTypes().values())
        {
            domainType.postProcess(applicationModel);
        }

        for (DomainRule domainRule : applicationModel.getDomainRules().values())
        {
            domainRule.postProcess(applicationModel);
        }

        for (Process process : applicationModel.getProcesses().values())
        {
            process.postProcess(applicationModel);
        }

        validateProcesses(applicationModel);

        final ApplicationMetaData metaData = applicationModel.getMetaData();

        metaData.postProcess();

        for (Process process : applicationModel.getProcesses().values())
        {
            for (ProcessState processState : process.getStates().values())
            {
                if (!(processState instanceof ViewState))
                {
                    processState.postProcess();
                }
            }
        }

        for (View view : applicationModel.getViews().values())
        {
            postprocessView(applicationModel, view);
        }

        metaData.validate();
        metaData.initJsEnv(jsEnvironmentFactory);
    }



    private void validateProcesses(ApplicationModel applicationModel)
    {
        for (Process process : applicationModel.getProcesses().values())
        {
            Transition startTransition = process.getStartTransition();
            final String processName = process.getName();
            if (startTransition == null)
            {
                throw new IllegalStateException("Process '" + processName + "' defines no start transition");
            }

            ProcessState processState = process.getStates().get(startTransition.getTo());
            if (processState == null)
            {
                throw new IllegalStateException("Process '" + processName + "' defines a non existing start-state: '" + startTransition + "' does not exist");
            }

            final Map<String, View> views = applicationModel.getViews();
            for (ProcessState state : process.getStates().values())
            {
                if (state instanceof ViewState)
                {
                    final String viewName = state.getName();

                    final View view = views.get(
                        process.getProcessStateName(
                            state.getName()
                        )
                    );
                    if (view == null)
                    {
                        log.debug("Create default view for missing view '{}'", viewName);

                        applicationModel.addView(
                            buildDefaultView(
                                processName,
                                (ViewState) state,
                                viewName
                            )
                        );
                    }
                }
            }
        }
    }


    private View buildDefaultView(String processName, ViewState state, String viewName)
    {
        View view;
        view = new View();
        view.setProcessName(processName);
        view.setTitle("Default view '" + viewName + "'");
        view.setName(Process.getProcessStateName(processName, viewName));
        view.setIdentityGUID(UUID.randomUUID().toString());
        view.setVersionGUID(UUID.randomUUID().toString());
        final HashMap<String, ComponentModel> content = new HashMap<>();
        List<ComponentModel> buttons = new ArrayList<>();

        final Map<String, Transition> transitions = state.getTransitions();

        if (transitions != null)
        {
            for (String transitionName : transitions.keySet())
            {
                buttons.add(
                    component("TButton")
                        .withAttribute("text", "Transition: " + transitionName)
                        .withAttribute("transition", transitionName)
                        .getComponent()
                );
            }
        }

        content.put(
            View.MAIN,
            component("div")
                .withKids(
                    component("Heading")
                        .withAttribute("icon", "glyphicon-info-sign")
                        .withAttribute("value", "View '" + viewName + "'"),
                    component("Toolbar")
                        .withKids(
                            buttons
                        )
                )
                .getComponent()
        );
        view.setContent(content);
        return view;
    }


    public void postprocessView(ApplicationModel applicationModel, View view)
    {
        log.debug("Postprocess {}", view);

        try
        {
            final String domainType = view.getDomainType();
            if (domainType != null && applicationModel.getDomainTypes().get(domainType) == null)
            {
                throw new InconsistentModelException("Invalid domainType reference in View '" + view.getName() + "': '" + domainType + "' is not a valid domain type");
            }

            final ScopeMetaModel scopeMetaModel = applicationModel.getMetaData().getScopeMetaModel();

            applyLayout(view, applicationModel);
            ComponentUtil.updateComponentRegsAndParents(componentRegistry, view, null);

            final String processName = view.getProcessName();
            if (processName != null)
            {
                final Process process = applicationModel.getProcess(processName);
                final ProcessState processState = process.getStates().get(view.getLocalName());

                if (processState == null)
                {
                    throw new InconsistentModelException("No view state with the name '" + view.getName() + " in " + process);
                }

                scopeMetaModel.addDeclarations(processState);
                processState.postProcess();
            }
            else
            {
                scopeMetaModel.addDeclarations(view);
            }
            applicationModel.getMetaData().createPropertyTypes(view.getContextModel());
        }
        catch(Exception e)
        {
            throw new InconsistentModelException("Error postprocessing view '" + view.getName() + "' ( " + view.getResource() + ")", e);
        }
    }

    private void applyLayout(View view, ApplicationModel applicationModel)
    {
        final String layoutName = view.getLayout(applicationModel);

        if (layoutName != null)
        {
            final LayoutModel layout = applicationModel.getLayout(layoutName);
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


    /**
     * Visits top level models and merges them into the application model.
     */
    private static class TopLevelModelMerger
        implements TopLevelModelVisitor<Object, TopLevelModel>
    {
        private final ApplicationModel applicationModel;

        private final String path;

        private final DomainService domainService;


        public TopLevelModelMerger(ApplicationModel applicationModel, String path, DomainService domainService)
        {
            this.applicationModel = applicationModel;
            this.path = path;
            this.domainService = domainService;
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
            String processName = path.substring(nameStart, path.indexOf('/', nameStart));
            process.setName(processName);
            applicationModel.addProcess( process);
            return process;
        }


        @Override
        public TopLevelModel visit(View view, Object o)
        {
            if (path.startsWith(ModelLocationRules.PROCESS_MODEL_PREFIX))
            {
                view.setProcessName(path.substring(ModelLocationRules.PROCESS_MODEL_PREFIX.length(), path.indexOf("/view/")));
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


//        @Override
//        public TopLevelModel visit(DomainEditorViews domainEditorViews, Object o)
//        {
//            applicationModel.getMetaData().setDomainEditorViews(domainEditorViews);
//            return domainEditorViews;
//        }

    }
}
