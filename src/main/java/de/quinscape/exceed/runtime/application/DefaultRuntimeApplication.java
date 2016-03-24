package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.Shutdown;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.change.Timeout;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProviderPreparationException;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.service.DomainServiceFactory;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.service.StyleService;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public static final String UPDATE_ID_PARAM = "_xcd_id";

    public static final String UPDATE_VARS_PARAM = "_xcd_vars";

    private final static Logger log = LoggerFactory.getLogger(DefaultRuntimeApplication.class);

    private final ServletContext servletContext;

    private final ApplicationModel applicationModel;

    private final ViewDataService viewDataService;

    private final StyleService styleService;

    private final ComponentRegistry componentRegistry;

    private final ModelCompositionService modelCompositionService;

    private final ResourceLoader resourceLoader;

    private final DomainService domainService;

    private final List<RuntimeInfoProvider> runtimeInfoProviders;

    private long lastChange;

    private Model changeModel = null;

    public final static String RUNTIME_INFO_NAME = "_exceed";

    public DefaultRuntimeApplication(
        ServletContext servletContext,
        ViewDataService viewDataService,
        ComponentRegistry componentRegistry,
        StyleService styleService,
        ModelCompositionService modelCompositionService,
        ResourceLoader resourceLoader,
        DomainServiceFactory domainServiceFactory,
        List<RuntimeInfoProvider> runtimeInfoProviders)
    {
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

        domainService = domainServiceFactory.create();
        modelCompositionService.compose(this, resourceLoader.getResourceLocations(), applicationModel);
        domainService.init(this, applicationModel.getSchema());

        modelCompositionService.postProcess(this, applicationModel);

        for (ResourceRoot root : resourceLoader.getExtensions())
        {
            ResourceWatcher resourceWatcher = root.getResourceWatcher();
            if (resourceWatcher != null)
            {
                resourceWatcher.register(this);
            }
        }
    }


    @Override
    public ServletContext getServletContext()
    {
        return servletContext;
    }


    @Override
    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public void route(HttpServletRequest request, HttpServletResponse response, RuntimeContext runtimeContext, ModelMap model) throws IOException
    {
        RoutingResult result = applicationModel.getRoutingTable().resolve(runtimeContext.getPath());


        String viewName = result.getMapping().getViewName();

        log.debug("Routing chose view '{}'", viewName);

        View view;
        if (viewName == null || (view = applicationModel.getViews().get(viewName)) == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "View " + viewName + " not found" +
                ".");
            return;
        }

        boolean isAjaxRequest = RequestUtil.isAjaxRequest(request);
        boolean isPreview = false;
        if (isAjaxRequest)
        {
            isPreview = isPreviewRequest(request);
            if (isPreview)
            {
                String json = RequestUtil.readRequestBody(request); ;
                View previewView = modelCompositionService.createViewModel(this, "preview/" + viewName, json, true);
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
        runtimeContext.setView(view);
        runtimeContext.setVariables(addRequestParameters(request, new HashMap<>(result.getVariables())));

        try
        {
            if (isAjaxRequest && isVarsUpdate(request))
            {
                updateComponentVars(runtimeContext, request, view, response);
                return;
            }

            ViewData viewData = viewDataService.prepareView(runtimeContext, view);

            Map<String, Object> viewDataMap = new HashMap<>(viewData.getData());
            viewDataMap.put(RUNTIME_INFO_NAME, getRuntimeInfo(request, runtimeContext));

            String viewDataJSON = domainService.toJSON(viewDataMap);
            String viewModelJSON = view.getCachedJSON();

            if (viewModelJSON == null)
            {
                throw new IllegalStateException("No view model JSON set in view");
            }


            if (!isAjaxRequest)
            {
                model.put("viewData", viewDataJSON);
                model.put("viewModel", viewModelJSON);
            }
            else
            {
                JSON gen = JSON.defaultJSON();
                String json = "{\n" +
                    "    \"appName\" : " + gen.forValue(model.get("appName")) + ",\n" +
                    "    \"title\" : " + gen.forValue(model.get("title")) + ",\n" +
                    "    \"viewData\" : " + viewDataJSON + ",\n" +
                    "    \"viewModel\": " + viewModelJSON + "\n" +
                    "}";

                RequestUtil.sendJSON(response, json);
            }
        }
        catch(DataProviderPreparationException e)
        {
            if (isPreview)
            {
                int index = ComponentUtil.findFlatIndex(view, e.getId());
                model.put("previewErrors", Collections.singletonList(new ProviderError(e.getCause().getMessage(), index)));
                RequestUtil.sendJSON(response, JSON.defaultJSON().forValue(model));
                return;
            }
            else
            {
                throw new ExceedRuntimeException("Error providing data to view", e);
            }
        }
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
        Map<String, Object> map = new HashMap<>();

        for (RuntimeInfoProvider provider : runtimeInfoProviders)
        {
            map.put(provider.getName(), provider.provide(request, runtimeContext));
        }

        return map;
    }


    private void updateComponentVars(RuntimeContext runtimeContext, HttpServletRequest request, View view, HttpServletResponse response) throws IOException
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
            vars);

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


    private int findComponentIndex(ComponentModel model, String componentId, int id)
    {
        if (componentId.equals(model.getComponentId()))
        {
            return id;
        }

        id++;

        List<ComponentModel> kids = model.getKids();
        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                id = findComponentIndex(kid, componentId, id);
            }
        }

        return id;
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
            else if (modulePath.endsWith(FileExtension.JSON))
            {
                TopLevelModel model = modelCompositionService.update(this, applicationModel, topResource);
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


    @Override
    public DomainService getDomainService()
    {
        return domainService;
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

}

