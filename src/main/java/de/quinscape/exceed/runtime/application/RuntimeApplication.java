package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.Application;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.Shutdown;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.change.Timeout;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.MappingNode;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.QueryResult;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.query.QueryField;
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
import de.quinscape.exceed.runtime.service.StyleService;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.view.ComponentData;
import de.quinscape.exceed.runtime.view.ViewData;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class RuntimeApplication
    implements ResourceChangeListener
{
    private static Logger log = LoggerFactory.getLogger(RuntimeApplication.class);

    private final ServletContext servletContext;

    private final Application applicationModel;

    private final ViewDataService viewDataService;

    private final StyleService styleService;

    private final ComponentRegistry componentRegistry;

    private final ModelCompositionService modelCompositionService;

    private final ResourceLoader resourceLoader;

    private final DomainService domainService;

    private long lastChange;

    private TopLevelModel changeModel = null;


    public RuntimeApplication(
        ServletContext servletContext,
        ViewDataService viewDataService,
        ComponentRegistry componentRegistry,
        StyleService styleService,
        ModelCompositionService modelCompositionService,
        ResourceLoader resourceLoader,
        DomainServiceFactory domainServiceFactory
    )
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

        //boolean production = ApplicationStatus.from(state) == ApplicationStatus.PRODUCTION;
        applicationModel = new Application();

        domainService = domainServiceFactory.create();
        modelCompositionService.compose(this, resourceLoader.getResourceLocations(), applicationModel);
        domainService.init(this, applicationModel.getSchema());

        for (ResourceRoot root : resourceLoader.getExtensions())
        {
            ResourceWatcher resourceWatcher = root.getResourceWatcher();
            if (resourceWatcher != null)
            {
                resourceWatcher.register(this);
            }
        }

    }


    public ServletContext getServletContext()
    {
        return servletContext;
    }


    public Application getApplicationModel()
    {
        return applicationModel;
    }


    public void route(RuntimeContext runtimeContext) throws IOException
    {
        RoutingResult result = resolve(runtimeContext.getPath());

        String viewName = result.getMapping().getViewName();

        log.debug("Routing chose view '{}'", viewName);

        View view;
        if (viewName == null || (view = applicationModel.getViews().get(viewName)) == null)
        {
            runtimeContext.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "View " + viewName + " not found" +
                ".");
            return;
        }

        runtimeContext.setView(view);

        ViewData viewData = viewDataService.prepareView(runtimeContext, view);

        ModelMap model = runtimeContext.getModel();
        model.put("viewData", domainService.toJSON(runtimeContext, viewData));
        model.put("viewModel", view.getCachedJSON());
    }


    private RoutingResult resolve(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");

        MappingNode node = applicationModel.getRoutingTable().getRootNode();

        while (node != null && tokenizer.hasMoreTokens())
        {
            String part = tokenizer.nextToken();

            MappingNode found = null;
            MappingNode varNode = null;
            for (MappingNode kid : node.children())
            {
                if (kid.getName().equals(part))
                {
                    found = kid;
                    break;
                }
                if (kid.isVariable() && varNode == null)
                {
                    varNode = kid;
                }
            }

            if (found == null)
            {
                found = varNode;
            }

            node = found;
        }

        if (!tokenizer.hasMoreTokens() && node != null)
        {
            Mapping mapping = node.getMapping();
            if (mapping != null)
            {
                return new RoutingResult(mapping);
            }
        }

        throw new MappingNotFoundException("Could not find a valid mapping for path '" + path + "'");
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


    private void collectComponentStyles(Application applicationModel, StringBuilder out)
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


    private Set<String> findUsedComponents(Application applicationModel)
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
            else if (modulePath.endsWith(FileExtension.JS))
            {
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


    private synchronized void notifyChange(TopLevelModel changeModel)
    {
        log.debug("notifyChange", changeModel);

        this.lastChange = System.currentTimeMillis();
        this.changeModel = changeModel;
        this.notifyAll();
    }


    public synchronized TopLevelModel waitForChange(long timeout) throws InterruptedException
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
            modelCompositionService.updateComponentRegistrations(this, view.getRoot(), componentNames);
        }
    }


    public DomainType getDomainType(String type)
    {
        DomainType domainType = applicationModel.getDomainTypes().get(type);

        if (domainType == null)
        {
            throw new IllegalArgumentException("Invalid type '" + type + "'");
        }

        return domainType;
    }
}

