package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.Shutdown;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.change.Timeout;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.model.meta.ApplicationMetaData;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ContextUpdate;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.ComponentRegistration;
import de.quinscape.exceed.runtime.component.DataProviderPreparationException;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.process.TransitionInput;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.schema.SchemaService;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.scope.ApplicationContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.scope.ViewContext;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.service.ProcessService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.service.StyleService;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.LocationUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.view.ComponentData;
import de.quinscape.exceed.runtime.view.ViewData;
import de.quinscape.exceed.runtime.view.ViewDataService;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONParseException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The application at runtime. Goes beyond what is included in the {@link RuntimeApplication} interface which is mostly
 * for testability. Some internal services currently only work with this implementation
 *
 * @see de.quinscape.exceed.runtime.controller.ApplicationController
 * @see de.quinscape.exceed.runtime.controller.ResourceController
 * @see de.quinscape.exceed.runtime.controller.HotReloadController
 *
 */
public class DefaultRuntimeApplication
    implements ResourceChangeListener, RuntimeApplication
{
    public static final String PREVIEW_HEADER_NAME = "X-ceed-Preview";

    public static final String UPDATE_HEADER_NAME = "X-ceed-Update";

    public static final String UPDATE_ID_PARAM = "_id";

    public static final String UPDATE_VARS_PARAM = "_vars";

    public static final String TRANSITION_PARAM = "_trans";

    private final static Logger log = LoggerFactory.getLogger(DefaultRuntimeApplication.class);

    private static final String SYSTEM_CONTEXT_PATH = "/sys";


    public static final String STATE_ID_PARAMETER = "stateId";

    private static final String CONTEXT_UPDATE_PARAM = "contextUpdate";

    private final ServletContext servletContext;

    private final ApplicationModel applicationModel;

    private final ViewDataService viewDataService;

    private final StyleService styleService;

    private final ComponentRegistry componentRegistry;

    private final ModelCompositionService modelCompositionService;

    private final ResourceLoader resourceLoader;

    private final DomainService domainService;

    private final ProcessService processService;

    private final ApplicationContext applicationContext;

    private final ScopedContextFactory scopedContextFactory;

    private final ClientStateService clientStateService;

    private final NashornScriptEngine nashorn;

    private final Definitions systemDefinitions;

    private long lastChange;

    private final RuntimeContextFactory runtimeContextFactory;

    private final Set<ClientStateProvider> clientStateProviders;

    private Model changeModel = null;

    private ResourceInjector resourceInjector = new ResourceInjector(ApplicationMetaData.class);

    public DefaultRuntimeApplication(
        ServletContext servletContext,
        ViewDataService viewDataService,
        ComponentRegistry componentRegistry,
        StyleService styleService,
        ModelCompositionService modelCompositionService,
        ResourceLoader resourceLoader,
        DomainService domainService,
        ClientStateService clientStateService,
        ProcessService processService,
        String appName,
        RuntimeContextFactory runtimeContextFactory,
        ScopedContextFactory scopedContextFactory,
        StorageConfigurationRepository storageConfigurationRepository,
        Set<ClientStateProvider> clientStateProviders,
        NashornScriptEngine nashorn,
        Definitions definitions
    )
    {
        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }

        this.processService = processService;
        this.scopedContextFactory = scopedContextFactory;
        this.runtimeContextFactory = runtimeContextFactory;
        this.clientStateProviders = clientStateProviders;

        this.servletContext = servletContext;
        this.viewDataService = viewDataService;
        this.styleService = styleService;
        this.componentRegistry = componentRegistry;
        this.modelCompositionService = modelCompositionService;
        this.resourceLoader = resourceLoader;
        this.clientStateService = clientStateService;
        this.systemDefinitions = definitions;

        //boolean production = ApplicationStatus.from(state) == ApplicationStatus.PRODUCTION;

        this.domainService = domainService;
        applicationModel = modelCompositionService.compose(resourceLoader.getAllResources().values(), domainService, systemDefinitions, appName);

        ContextModel context = applicationModel.getApplicationContextModel();

        this.applicationContext = scopedContextFactory.createApplicationContext(context, appName);

        final ApplicationMetaData metaData = this.applicationModel.getMetaData();
        resourceInjector.injectResources(nashorn, resourceLoader, metaData);

        modelCompositionService.postprocess(applicationModel);
        
        RuntimeContext systemContext = createSystemContext();
        RuntimeContextHolder.register(systemContext, null);
        scopedContextFactory.initializeContext(applicationModel.getMetaData().getJsEnvironment(), systemContext, applicationContext);
        domainService.init(this, applicationModel.getConfigModel().getSchema());

        for (ResourceRoot root : resourceLoader.getExtensions())
        {
            ResourceWatcher resourceWatcher = root.getResourceWatcher();
            if (resourceWatcher != null)
            {
                log.debug("Register lister for watcher of resource root {}", root);
                resourceWatcher.register(this);
            }
            else
            {
                log.debug("Non-reloadable resource root {}", root);
            }
        }

        synchronizeDomainTypeSchemata(systemContext, storageConfigurationRepository);

        this.nashorn = nashorn;     
    }


    public RuntimeContext createSystemContext()
    {
        return this.runtimeContextFactory.create(
            this,
                SYSTEM_CONTEXT_PATH,
                Locale.forLanguageTag("en-US"),
                new ScopedContextChain(
                    Collections.singletonList(
                        applicationContext
                    ),
                    applicationModel.getMetaData().getScopeMetaModel(),
                    ScopeMetaModel.SYSTEM
                ),
                this.domainService
            );
    }


    private void synchronizeDomainTypeSchemata(RuntimeContext systemContext, StorageConfigurationRepository
        storageConfigurationRepository)
    {
        final Map<String, List<DomainType>> map = mapDomainTypesByStorageConfig(systemContext);

        for (Map.Entry<String, List<DomainType>> entry : map.entrySet())
        {
            final SchemaService schemaService = storageConfigurationRepository.getConfiguration(entry.getKey()).getSchemaService();
            if (schemaService != null)
            {
                schemaService.synchronizeSchema(systemContext, entry.getValue());
            }
        }
    }


    private Map<String, List<DomainType>> mapDomainTypesByStorageConfig(RuntimeContext systemContext)
    {
        Map<String,List<DomainType>> bySchemaService = new HashMap<>();
        for (DomainType type : systemContext.getDomainService().getDomainTypes().values())
        {
            final String storageConfig = type.getStorageConfiguration();

            List<DomainType> domainTypes = bySchemaService.get(storageConfig);
            if (domainTypes == null)
            {
                domainTypes = new ArrayList<>();
                bySchemaService.put(storageConfig, domainTypes);
            }

            domainTypes.add(type);
        }

        return bySchemaService;
    }


    @Override
    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public boolean route(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, String inAppURI) throws Exception
    {
        RoutingResult result = applicationModel.getRoutingTable().resolve(inAppURI);
        Mapping mapping = result.getMapping();
        final Map<String, String> variables = result.getVariables();
        final String template = result.getTemplate();
        String viewName = mapping.getViewName();
        String processName = mapping.getProcessName();

        return processView(request, response, model, inAppURI, variables, template, viewName, processName);
    }


    public boolean  processView(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model,
                               String inAppURI, Map<String, String> variables, String template, final String viewName, final String processName) throws IOException, ParseException
    {
        SessionContext sessionContext = null;
        try
        {
            sessionContext = scopedContextFactory.getSessionContext(
                request,
                applicationModel.getName(),
                applicationModel.getSessionContextModel()
            );
            
            RuntimeContext runtimeContext = runtimeContextFactory.create(
                this, inAppURI,
                request.getLocale(),
                new ScopedContextChain(
                    Arrays.asList(
                        applicationContext,
                        sessionContext
                    )
                    , applicationModel.getMetaData().getScopeMetaModel(), null),
                domainService);

            RuntimeContextHolder.register(runtimeContext, request);
            scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, sessionContext);


            runtimeContext.setVariables(addRequestParameters(request, new HashMap<>(variables)));
            runtimeContext.setRoutingTemplate(template);

            View view;
            final boolean isAjaxRequest = RequestUtil.isAjaxRequest(request);
            final boolean isPreview = isAjaxRequest && isPreviewRequest(request);
            final boolean isComponentUpdate = isAjaxRequest && isComponentUpdate(request);

            TransitionInput transitionData = null;

            ProcessExecutionState state = null;
            if (processName != null)
            {
                Process process = applicationModel.getProcess(processName);
                String stateId = (String) runtimeContext.getLocationParams().get(STATE_ID_PARAMETER);

                boolean redirect;
                if (stateId == null)
                {
                    log.debug("Start process {}", process.getName());

                    state = processService.start(runtimeContext, process);
                    state.register(request.getSession());
                    redirect = true;
                }
                else
                {
                    ProcessExecutionState initialState = ProcessExecutionState.lookup(request.getSession(), stateId);
                    if (initialState == null)
                    {
                        throw new StateNotFoundException("State '" + stateId + "' does not exist");
                    }

                    String transition = request.getParameter(TRANSITION_PARAM);
                    if (transition != null)
                    {
                        log.debug("Process state {}, transition = {}", stateId, transition);
                        final String json = RequestUtil.readRequestBody(request);

                        transitionData = TransitionInput.parse(runtimeContext, initialState.getExecution().getProcessName(), initialState.getCurrentState(), json);
                        state = processService.resume(runtimeContext, initialState, transition, transitionData);
                        state.register(request.getSession());

                        runtimeContext.setVariable(STATE_ID_PARAMETER, state.getId());

                        redirect = !isAjaxRequest;
                    }
                    else
                    {
                        log.debug("(Re)render state {}", stateId);
                        state = initialState;

                        runtimeContext.getScopedContextChain().update(state.getScopedContext(), Process.getProcessStateName(processName, state.getCurrentState()));

                        redirect = false;
                    }
                }

                if (redirect)
                {
                    Map<String, Object> params = runtimeContext.getLocationParams();
                    // request ends here anyway, can change in-place
                    params.put("stateId", state.getId());
                    params.remove(TRANSITION_PARAM);

                    response.sendRedirect(request.getContextPath() + "/app/" + applicationModel.getName() + LocationUtil.evaluateURI(template, params));
                    return true;
                }

                ProcessState processState = process.getStates().get(state.getCurrentState());
                if (processState == null)
                {
                    throw new IllegalStateException("Process state '" + state.getCurrentState() + "' does not exist " +
                        "in " + process);
                }

                String processViewName = Process.getProcessStateName(state.getExecution().getProcessName(), state.getCurrentState());
                view = applicationModel.getView(processViewName);

            }
            else if (viewName != null)
            {
                log.debug("Routing chose view '{}'", viewName);
                view = applicationModel.getView(viewName);
            }
            else
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found.");
                return true;
            }

            if (view == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "View " + viewName + " not found" +
                    ".");
                return true;
            }

            // process service will have already initialized a view context unless a start state is redirect-after-posted to.
            if (runtimeContext.getScopedContextChain().getViewContext() == null)
            {
                final ViewContext viewContext = scopedContextFactory.createViewContext(view);
                scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, viewContext);
                runtimeContext.getScopedContextChain().update(viewContext, view.getName());
            }

            Map<String,Object> postData = Collections.emptyMap();
            if ( transitionData == null && (isPreview || isComponentUpdate))
            {
                postData = (Map<String, Object>) JSONUtil.DEFAULT_PARSER.parse(Map.class, RequestUtil.readRequestBody(request));
            }


            if (isAjaxRequest)
            {
                if (isPreview)
                {
                    Map<String,String> changes;
                    if (transitionData != null)
                    {
                        changes = transitionData.getChangedViewModels();
                    }
                    else
                    {
                        changes = (Map<String, String>) postData.get("changedViewModels");
                    }

                    // check if the current view is in the changed view set
                    String viewJSON = changes.get(view.getName());
                    if (viewJSON != null)
                    {
                        // yes -> use the preview view instead of the stored view
                        
                        View previewView = modelCompositionService.createPreviewViewModel(view.getResource(), viewJSON);
                        if (!Objects.equals(view.getName(), previewView.getName()))
                        {
                            throw new IllegalStateException(
                                "Cannot preview different view. view = " + view + ", preview = " + previewView);
                        }
                        previewView.setProcessName(view.getProcessName());
                        modelCompositionService.postprocessView(runtimeContext.getApplicationModel(), previewView);

                        List<ComponentError> errors = new ArrayList<>();

                        for (Map.Entry<String, ComponentModel> entry : previewView.getContent().entrySet())
                        {
                            collectErrors(errors, entry.getKey(), entry.getValue(), 0);
                        }
                        
                        if (errors.size() > 0)
                        {
                            RequestUtil.sendJSON(response, JSONUtil.error("Preview error", errors));
                            return true;
                        }
                        view = previewView;
                    }
                }
            }

            if (isAjaxRequest && isComponentUpdate(request))
            {
                updateComponentVars(runtimeContext, request, response, state, view, postData);
                return true;
            }

            final String viewDataJSON;
            try
            {
                viewDataJSON = processView(request, runtimeContext, view, state);
            }
            catch (DataProviderPreparationException e)
            {
                if (isPreview)
                {
                    int index = ComponentUtil.findFlatIndex(view, e.getId());
                    model.put("previewErrors", Collections.singletonList(new ProviderError(e.getCause().getMessage(),
                        index)));
                    RequestUtil.sendJSON(response, JSONUtil.DEFAULT_GENERATOR.forValue(model));
                    return true;
                }
                else
                {
                    throw new ExceedRuntimeException("Error providing data to view", e);
                }
            }


            if (!isAjaxRequest)
            {
                model.put(TemplateVariables.VIEW_DATA, viewDataJSON);
                return false;
            }
            else
            {

                RequestUtil.sendJSON(response, viewDataJSON);
                return true;
            }
        }
        finally
        {
            scopedContextFactory.updateSessionContext(request, applicationModel.getName(), sessionContext);
            scopedContextFactory.updateApplicationContext(applicationModel.getName(), applicationContext, domainService);
        }
    }


    public String processView(HttpServletRequest request, RuntimeContext runtimeContext, View view, ProcessExecutionState state) throws IOException
    {
        runtimeContext.setView(view);

        ViewData viewData = viewDataService.prepareView(runtimeContext, view, state);
        return clientStateService.getClientStateJSON(request, runtimeContext, viewData, clientStateProviders);
    }


    private Map<String, Object> addRequestParameters(HttpServletRequest request, Map<String, Object> params)
    {

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet())
        {
            String name = entry.getKey();
            String[] values = entry.getValue();

            if (values.length == 1)
            {
                params.putIfAbsent(name, values[0]);
            }
            else
            {
                params.putIfAbsent(name, values);
            }
        }
        return params;
    }



    private void updateComponentVars(RuntimeContext runtimeContext, HttpServletRequest request, HttpServletResponse response, ProcessExecutionState state, View view, Map<String, Object> postData) throws IOException, ParseException

    {
        String componentId = (String) postData.get(UPDATE_ID_PARAM);
        Map<String,Object> vars = (Map<String, Object>) postData.get(UPDATE_VARS_PARAM);
        Map<String,Object> contextUpdates = (Map<String, Object>) postData.get(CONTEXT_UPDATE_PARAM);

        if (componentId == null)
        {
            throw new IllegalStateException("componentId can't be null");
        }

        if (vars == null)
        {
            throw new IllegalStateException("vars can't be null");
        }


        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        Process process = state != null ? applicationModel.getProcess(state.getExecution().getProcessName()) : null;
        

        if (contextUpdates != null)
        {
            ContextUpdate.convertToJava(runtimeContext, process,  view, contextUpdates);
            runtimeContext.getScopedContextChain().update(contextUpdates);
        }

        ComponentModel componentModel = view.find((m) -> {
            ExpressionValue value = m.getAttribute(DomainType.ID_PROPERTY);
            return value != null && componentId.equals(value.getValue());
        });

        if (componentModel == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Component '" + componentId + "' not found");
            return;
        }

        ComponentData componentData = viewDataService.prepareComponent(runtimeContext, view, componentModel,
            vars, state);

        RequestUtil.sendJSON(response, domainService.toJSON(componentData));
    }


    private Map parseVars(String varsJSON)
    {
        try
        {
            return JSONUtil.DEFAULT_PARSER.parse(Map.class, varsJSON);
        }
        catch(JSONParseException e)
        {
            throw new ExceedRuntimeException("Error parsing " + varsJSON, e);
        }
    }


    private static boolean isPreviewRequest(HttpServletRequest request)
    {
         return request.getMethod().equals("POST") && "true".equals(request.getHeader(PREVIEW_HEADER_NAME));
    }

    private static boolean isComponentUpdate(HttpServletRequest request)
    {
         return "true".equals(request.getHeader(UPDATE_HEADER_NAME));
    }

    private int collectErrors(List<ComponentError> errors, String contentName, ComponentModel model, int id)
    {
        Attributes attrs = model.getAttrs();
        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                ExpressionValue attributeValue = attrs.getAttribute(name);
                if (attributeValue.getType() == ExpressionValueType.EXPRESSION_ERROR)
                {
                    errors.add(new ComponentError(contentName, attributeValue.getValue(), attributeValue.getExpressionError(), id, name));
                }
            }
        }

        id++;

        List<ComponentModel> kids = model.getKids();
        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                id = collectErrors(errors, contentName, kid, id);
            }
        }

        return id;
    }


    public String getCollectedStyles()
    {
        try
        {
            StringBuilder sb = new StringBuilder();


            for (String name : applicationModel.getConfigModel().getStyleSheets())
            {
                PathResources resourceLocation = resourceLoader.getResources(name);

                if (resourceLocation == null)
                {
                    log.info("RESOURCE LOCATIONS:\n{}", JSON.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(resourceLoader
                        .getAllResources().keySet())));
                    throw new IllegalStateException("Resource '" + name + "' does not exist in any extension");
                }

                ResourceRoot root = resourceLocation.getHighestPriorityResource().getResourceRoot();
                sb.append("/* APP '")
                    .append(name)
                    .append("' */\n")
                    .append(styleService.process(root, name))
                    .append('\n');
            }

            collectComponentStyles(applicationModel, sb);

            return sb.toString();
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private void collectComponentStyles(ApplicationModel applicationModel, StringBuilder out)
    {
        Set<String> usedComponents = findUsedComponents(applicationModel);
        for (String name : usedComponents)
        {
            if (Character.isUpperCase(name.charAt(0)))
            {
                ComponentRegistration registration = componentRegistry
                    .getComponentRegistration(name);

                if (registration == null)
                {
                    throw new IllegalStateException("No component registration with name '" + name + "' found");
                }


                String styles = registration.getStyles();
                if (styles != null)
                {
                    out.append("/* COMPONENT '")
                        .append(name)
                        .append("' */\n")
                        .append(styles)
                        .append('\n');
                }
            }
        }
    }


    private Set<String> findUsedComponents(ApplicationModel applicationModel)
    {
        Set<String> usedComponents = new HashSet<>();
        for (View view : applicationModel.getViews().values())
        {
            for (ComponentModel componentModel : view.getContent().values())
            {
                addComponentsRecursive(componentModel, usedComponents);
            }
        }

        return usedComponents;
    }


    private void addComponentsRecursive(ComponentModel component, Set<String> usedComponents)
    {
        final String componentName = component.getName();
        if (!usedComponents.contains(componentName))
        {
            usedComponents.add(componentName);
        }
        
        for (ComponentModel kid : component.children())
        {
            addComponentsRecursive(kid, usedComponents);
        }
    }


    @Override
    public synchronized void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String
        modulePath)
    {
        log.debug("onResourceChange:  {} {} ( ROOT {} ) ", resourceEvent, modulePath, root);

        PathResources resourceLocation = resourceLoader.getResources(modulePath);

        if (resourceLocation == null)
        {
            return;
        }

        AppResource topResource = resourceLocation.getHighestPriorityResource();
        ResourceRoot rootOfTopResource = topResource.getResourceRoot();
        if (root.equals(rootOfTopResource))
        {
            if (modulePath.endsWith(FileExtension.CSS))
            {
                if (applicationModel.getConfigModel().getStyleSheets().contains(modulePath))
                {
                    try
                    {
                        styleService.reload(root, modulePath);
                    }
                    catch (IOException e)
                    {
                        throw new ExceedRuntimeException(e);
                    }
                    notifyStyleChange();
                }
            }
            else if (modulePath.endsWith(FileExtension.JSON))
            {
                TopLevelModel model = modelCompositionService.update(applicationModel, domainService, topResource);
                if (model != null)
                {
                    notifyChange(model);

                    if (model instanceof View)
                    {
                        clientStateService.flushViewScope((View) model);
                    }
                    clientStateService.flushModelVersionScope(getName());
                }

                resourceInjector.updateResource(nashorn, resourceLoader, applicationModel.getMetaData(), modulePath);
            }
            else if (modulePath.equals("/resources/js/main.js"))
            {
                log.debug("Reload js: {}", modulePath);
                notifyCodeChange();
            }
            else
            {
                resourceInjector.updateResource(nashorn, resourceLoader, applicationModel.getMetaData(), modulePath);
            }
        }
    }


    public void notifyStyleChange()
    {
        log.debug("notifyStyleChange");

        notifyChange(StyleChange.INSTANCE);
    }


    public void notifyCodeChange()
    {
        notifyChange(CodeChange.INSTANCE);
    }


    private synchronized void notifyChange(Model changeModel)
    {
        log.debug("notifyChange", changeModel);

        this.lastChange = System.currentTimeMillis();
        this.changeModel = changeModel;
        this.notifyAll();
    }


    public synchronized Model waitForChange(long timeout) throws InterruptedException
    {
        long start = lastChange;

        while (lastChange == start)
        {
            this.wait(timeout);
            if (lastChange == start)
            {
                return Timeout.INSTANCE;
            }
        }
        return changeModel;
    }


    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }


    public void notifyShutdown()
    {
        notifyChange(Shutdown.INSTANCE);
    }


    public void signalComponentChanges(Set<String> componentNames)
    {
        for (View view : applicationModel.getViews().values())
        {
            ComponentUtil.updateComponentRegsAndParents(componentRegistry, view, componentNames);
        }
    }

    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }
}

