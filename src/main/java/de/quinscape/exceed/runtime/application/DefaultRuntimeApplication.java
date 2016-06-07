package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.Shutdown;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.change.Timeout;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.DataProviderPreparationException;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.scope.ApplicationContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.service.ProcessService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.service.StyleService;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.LocationUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.view.ComponentData;
import de.quinscape.exceed.runtime.view.ViewData;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.svenson.JSON;
import org.svenson.JSONParseException;
import org.svenson.JSONParser;
import org.svenson.util.JSONBuilder;

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

    public static final String TRACK_USAGE_DATA_RESOURCE = "/resources/js/track-usage.json";

    public static final String STATE_ID_PARAMETER = "stateId";

    private final ServletContext servletContext;

    private final ApplicationModel applicationModel;

    private final ViewDataService viewDataService;

    private final StyleService styleService;

    private final ComponentRegistry componentRegistry;

    private final ModelCompositionService modelCompositionService;

    private final ResourceLoader resourceLoader;

    private final DomainService domainService;

    private final List<RuntimeInfoProvider> runtimeInfoProviders;

    private final ProcessService processService;

    private final ApplicationContext applicationContext;

    private final ScopedContextFactory scopedContextFactory;

    private long lastChange;

    public final static String RUNTIME_INFO_NAME = "_exceed";

    private final RuntimeContextFactory runtimeContextFactory;

    private Model changeModel = null;

    public DefaultRuntimeApplication(
        ServletContext servletContext,
        ViewDataService viewDataService,
        ComponentRegistry componentRegistry,
        StyleService styleService,
        ModelCompositionService modelCompositionService,
        ResourceLoader resourceLoader,
        DomainService domainService,
        List<RuntimeInfoProvider> runtimeInfoProviders,
        ProcessService processService,
        String appName,
        RuntimeContextFactory runtimeContextFactory,
        ScopedContextFactory scopedContextFactory)
    {
        this.processService = processService;
        this.scopedContextFactory = scopedContextFactory;
        this.runtimeContextFactory = runtimeContextFactory;
        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }

        this.servletContext = servletContext;
        this.viewDataService = viewDataService;
        this.styleService = styleService;
        this.componentRegistry = componentRegistry;
        this.modelCompositionService = modelCompositionService;
        this.resourceLoader = resourceLoader;
        this.runtimeInfoProviders = runtimeInfoProviders;

        //boolean production = ApplicationStatus.from(state) == ApplicationStatus.PRODUCTION;
        applicationModel = new ApplicationModel();
        applicationModel.setName(appName);

        this.domainService = domainService;
        modelCompositionService.compose(this, resourceLoader.getResourceLocations(), applicationModel, domainService);
        domainService.init(this, applicationModel.getSchema());

        ContextModel context = applicationModel.getApplicationContext();

        this.applicationContext = scopedContextFactory.createApplicationContext(context, appName);
        RuntimeContext systemContext = runtimeContextFactory.create(
            this,
            SYSTEM_CONTEXT_PATH,
            Locale.forLanguageTag("en-US"),
            new ScopedContextChain(Collections.singletonList(applicationContext)),
            domainService);

        scopedContextFactory.initializeContext(systemContext, applicationContext);

        modelCompositionService.postProcess(this, applicationModel);

        for (ResourceRoot root : resourceLoader.getExtensions())
        {
            ResourceWatcher resourceWatcher = root.getResourceWatcher();
            if (resourceWatcher != null)
            {
                resourceWatcher.register(this);
            }
        }

        final StaticFunctionReferences staticFnRefs = loadUsageData(
            resourceLoader.getResourceLocation(TRACK_USAGE_DATA_RESOURCE).getHighestPriorityResource()
        );
        this.applicationModel.setStaticFunctionReferences(staticFnRefs);
    }


    @Override
    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public void route(HttpServletRequest request, HttpServletResponse response, ModelMap model, String inAppURI) throws Exception
    {
        SessionContext sessionContext = null;
        try
        {

            sessionContext = scopedContextFactory.getSessionContext(
                request,
                applicationModel.getName(),
                applicationModel.getSessionContext()
            );
            RuntimeContext runtimeContext = runtimeContextFactory.create(
                this, inAppURI,
                request.getLocale(),
                new ScopedContextChain(
                    Arrays.asList(
                        applicationContext,
                        sessionContext
                    )
                ),
                domainService);

            RuntimeContextHolder.register(runtimeContext);
            scopedContextFactory.initializeContext(runtimeContext, sessionContext);

            RoutingResult result = applicationModel.getRoutingTable().resolve(inAppURI);

            Mapping mapping = result.getMapping();

            runtimeContext.setVariables(addRequestParameters(request, new HashMap<>(result.getVariables())));
            runtimeContext.setRoutingTemplate(result.getTemplate());

            String viewName = mapping.getViewName();
            String processName = mapping.getProcessName();

            View view;
            boolean isAjaxRequest = RequestUtil.isAjaxRequest(request);
            boolean isPreview = isAjaxRequest && isPreviewRequest(request);

            ProcessExecutionState state = null;
            if (processName != null)
            {
                Process process = applicationModel.getProcess(processName);
                String stateId = (String) runtimeContext.getLocationParams().get(STATE_ID_PARAMETER);

                boolean redirect;
                if (stateId != null)
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
                        state = processService.resume(runtimeContext, initialState, transition, RequestUtil.readRequestBody(request));
                        state.register(request.getSession());

                        runtimeContext.setVariable(STATE_ID_PARAMETER, state.getId());

                        redirect = !isAjaxRequest;
                    }
                    else
                    {
                        log.debug("(Re)render state {}", stateId);
                        state = initialState;
                        runtimeContext.getScopedContextChain().addContext(state.getScopedContext());

                        redirect = false;
                    }
                }
                else
                {
                    log.debug("Start process {}", process.getName());

                    state = processService.start(runtimeContext, process);
                    state.register(request.getSession());
                    redirect = true;
                }

                if (redirect)
                {
                    Map<String, Object> params = runtimeContext.getLocationParams();
                    // request ends here anyway, can change in-place
                    params.put("stateId", state.getId());
                    params.remove(TRANSITION_PARAM);

                    response.sendRedirect(request.getContextPath() + "/app/" + applicationModel.getName() + LocationUtil.evaluateURI(result.getTemplate(), params));

                    return;
                }

                ProcessState processState = process.getStates().get(state.getCurrentState());
                if (processState == null)
                {
                    throw new IllegalStateException("Process state '" + state.getCurrentState() + "' does not exist " +
                        "in " + process);
                }

                String processViewName = Process.getProcessViewName(state.getExecution().getProcessName(), state.getCurrentState());
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
                return;
            }

            if (view == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "View " + viewName + " not found" +
                    ".");
                return;
            }

            if (isAjaxRequest && isVarsUpdate(request))
            {
                updateComponentVars(runtimeContext, request, response, state, view);
                return;
            }

            ViewResult data;
            if (isAjaxRequest)
            {
                if (isPreview)
                {
                    String json = RequestUtil.readRequestBody(request);

                    View previewView = modelCompositionService.createViewModel(this, view.getResource(), json, true);
                    if (!Objects.equals(view.getName(), previewView.getName()))
                    {
                        throw new IllegalStateException("Cannot preview different view. view = " + view + ", preview " +
                            "= " + previewView);
                    }
                    previewView.setProcessName(view.getProcessName());
                    modelCompositionService.postProcessView(this, previewView);

                    List<ComponentError> errors = new ArrayList<>();

                    collectErrors(errors, previewView.getRoot(), 0);
                    if (errors.size() == 0)
                    {
                        view = previewView;
                    }
                    else
                    {
                        model.put("previewErrors", errors);
                        RequestUtil.sendJSON(response, JSON.defaultJSON().forValue(model));
                        return;
                    }
                }
            }


            try
            {
                data = processView(request, response, runtimeContext, model, view, state);
            }
            catch (DataProviderPreparationException e)
            {
                if (isPreview)
                {
                    int index = ComponentUtil.findFlatIndex(view, e.getId());
                    model.put("previewErrors", Collections.singletonList(new ProviderError(e.getCause().getMessage(),
                        index)));
                    RequestUtil.sendJSON(response, JSON.defaultJSON().forValue(model));
                    return;
                }
                else
                {
                    throw new ExceedRuntimeException("Error providing data to view", e);
                }
            }

            if (!isAjaxRequest)
            {
                model.put("viewModel", data.rootModelJSON);
                model.put("viewData", data.viewDataJSON);
            }
            else
            {
                String json =
                    JSONBuilder.buildObject()
                        .property("appName", model.get(applicationModel.getName()))
                        .property("title", model.get("title"))
                        .includeProperty("viewModel", data.rootModelJSON)
                        .includeProperty("viewData", data.viewDataJSON)
                        .output();

                RequestUtil.sendJSON(response, json);
            }
        }
        finally
        {
            scopedContextFactory.updateSessionContext(request, applicationModel.getName(), sessionContext);
            scopedContextFactory.updateApplicationContext(applicationModel.getName(), applicationContext, domainService);
        }
    }


    private DomainObject getTransitionDomainContext(RuntimeContext runtimeContext, String json) throws ParseException
    {
        DomainObject partialDomainObjectContext;
        log.debug("Domain Object context: {}", json);

        partialDomainObjectContext = domainService.toDomainObject(GenericDomainObject.class, json);

        if (partialDomainObjectContext != null && partialDomainObjectContext.getDomainType() == null)
        {
            partialDomainObjectContext = null;
        }
        partialDomainObjectContext = DomainUtil.convertToJava(runtimeContext, partialDomainObjectContext);

        log.debug("Partial object context: {}", partialDomainObjectContext);
        return partialDomainObjectContext;
    }


    private ViewResult processView(HttpServletRequest request, HttpServletResponse response, RuntimeContext runtimeContext, ModelMap model, View view, ProcessExecutionState state) throws IOException
    {
        runtimeContext.setView(view);

        ViewData viewData = viewDataService.prepareView(runtimeContext, view, state);

        Map<String, Object> viewDataMap = new HashMap<>(viewData.getData());
        viewDataMap.put(RUNTIME_INFO_NAME, getRuntimeInfo(request, runtimeContext));

        String viewDataJSON = domainService.toJSON(viewDataMap);
        String viewModelJSON = view.getCachedJSON();

        if (viewModelJSON == null)
        {
            throw new IllegalStateException("No view model JSON set in view");
        }

        return new ViewResult(viewModelJSON, viewDataJSON);
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


    private Map<String, Object> getRuntimeInfo(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        try
        {
            Map<String, Object> map = new HashMap<>();

            for (RuntimeInfoProvider provider : runtimeInfoProviders)
            {
                map.put(provider.getName(), provider.provide(request, runtimeContext));
            }

            return map;
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private void updateComponentVars(RuntimeContext runtimeContext, HttpServletRequest request, HttpServletResponse response, ProcessExecutionState state, View view) throws IOException
    {
        String componentId = request.getParameter(UPDATE_ID_PARAM);
        String varsJSON = request.getParameter(UPDATE_VARS_PARAM);

        if (componentId == null)
        {
            throw new IllegalStateException("componentId can't be null");
        }

        if (varsJSON == null)
        {
            throw new IllegalStateException("varsJSON can't be null");
        }

        Map<String,Object> vars = parseVars(varsJSON);

        ComponentModel componentModel = view.find((m) -> {
            AttributeValue value = m.getAttribute("id");
            return value != null && componentId.equals(value.getValue());
        });

        ComponentData componentData = viewDataService.prepareComponent(runtimeContext, view, componentModel,
            vars, state);

        RequestUtil.sendJSON(response, domainService.toJSON(componentData));
    }


    private Map parseVars(String varsJSON)
    {
        try
        {
            return JSONParser.defaultJSONParser().parse(Map.class, varsJSON);
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

    private static boolean isVarsUpdate(HttpServletRequest request)
    {
         return "true".equals(request.getHeader(UPDATE_HEADER_NAME));
    }

    private int collectErrors(List<ComponentError> errors, ComponentModel model, int id)
    {
        Attributes attrs = model.getAttrs();
        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                AttributeValue attributeValue = attrs.getAttribute(name);
                if (attributeValue.getType() == AttributeValueType.EXPRESSION_ERROR)
                {
                    errors.add(new ComponentError((String) attributeValue.getValue(), attributeValue.getExpressionError(), id, name));
                }
            }
        }

        id++;

        List<ComponentModel> kids = model.getKids();
        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                id = collectErrors(errors, kid, id);
            }
        }

        return id;
    }


    public String getCollectedStyles()
    {
        try
        {
            StringBuilder sb = new StringBuilder();


            for (String name : applicationModel.getStyleSheets())
            {
                ResourceLocation resourceLocation = resourceLoader.getResourceLocation(name);

                if (resourceLocation == null)
                {
                    log.info("RESOURCE LOCATIONS:\n{}", JSON.formatJSON(JSON.defaultJSON().forValue(resourceLoader
                        .getResourceLocations().keySet())));
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
            addComponentsRecursive(view.getRoot(), usedComponents);
        }

        return usedComponents;
    }


    private void addComponentsRecursive(ComponentModel component, Set<String> usedComponents)
    {
        usedComponents.add(component.getName());
        for (ComponentModel kid : component.children())
        {
            addComponentsRecursive(kid, usedComponents);
        }
    }


    @Override
    public synchronized void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String
        modulePath)
    {
        log.debug("onResourceChange:  {}:{}", resourceEvent, root, modulePath);

        ResourceLocation resourceLocation = resourceLoader.getResourceLocation(modulePath);

        AppResource topResource = resourceLocation.getHighestPriorityResource();
        ResourceRoot rootOfTopResource = topResource.getResourceRoot();
        if (root.equals(rootOfTopResource))
        {
            if (modulePath.endsWith(FileExtension.CSS))
            {
                if (applicationModel.getStyleSheets().contains(modulePath))
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
            else if (modulePath.equals(TRACK_USAGE_DATA_RESOURCE))
            {
                final StaticFunctionReferences staticFnRefs = loadUsageData(topResource);
                this.applicationModel.setStaticFunctionReferences(staticFnRefs);
            }
            else if (modulePath.endsWith(FileExtension.JSON))
            {
                TopLevelModel model = modelCompositionService.update(this, applicationModel, topResource, domainService);
                if (model != null)
                {
                    notifyChange(model);
                }
            }
            else if (modulePath.equals("/resources/js/main.js"))
            {
                log.debug("Reload js: {}", modulePath);
                notifyCodeChange();
            }
        }
    }


    private StaticFunctionReferences loadUsageData(AppResource resource)
    {
        if (resource.exists())
        {
            String json = new String(resource.read(), RequestUtil.UTF_8);
            if (json.length() > 0)
            {
                log.info("Load function usage data");
                return JSONParser.defaultJSONParser().parse(StaticFunctionReferences.class, json);
            }
        }
        return null;
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

    private class ViewResult
    {
        public final String rootModelJSON;

        public final String viewDataJSON;

        public ViewResult(String viewModelJSON, String viewDataJSON)
        {
            this.rootModelJSON = viewModelJSON;
            this.viewDataJSON = viewDataJSON;
        }
    }


    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }
}

