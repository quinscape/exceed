package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.Application;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.MappingNode;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.view.ViewData;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

public class RuntimeApplication
{
    private static Logger log = LoggerFactory.getLogger(RuntimeApplication.class);

    private final ServletContext servletContext;

    private final Application applicationModel;

    private final String collectedStyles;

    private final ViewDataService viewDataService;


    public RuntimeApplication(
        ServletContext servletContext,
        Application applicationModel,
        String collectedStyles, ViewDataService viewDataService)
    {
        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }

        if (applicationModel == null)
        {
            throw new IllegalArgumentException("applicationModel can't be null");
        }

        this.servletContext = servletContext;
        this.applicationModel = applicationModel;
        this.collectedStyles = collectedStyles;
        this.viewDataService = viewDataService;
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
            runtimeContext.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "View " + viewName + " not found.");
            return;
        }

        ViewData viewData = viewDataService.prepareData(runtimeContext, view);

        ModelMap model = runtimeContext.getModel();
        model.put("viewData", JSON.defaultJSON().forValue(viewData));
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
        return collectedStyles;
    }
}

