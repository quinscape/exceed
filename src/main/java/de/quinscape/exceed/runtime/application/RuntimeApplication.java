package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.MappingNode;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

public class RuntimeApplication
{
    private static Logger log = LoggerFactory.getLogger(RuntimeApplication.class);

    private final ServletContext servletContext;

    private final ApplicationModel applicationModel;

    public RuntimeApplication(
        ServletContext servletContext,
        ApplicationModel applicationModel

    )
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
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public ApplicationModel getApplicationModel()
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
}

