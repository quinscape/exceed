package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.ApplicationModel;
import org.jooq.DSLContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RuntimeApplication
{
    private final ServletContext servletContext;
    private final ApplicationModel applicationModel;
    private final boolean production;

    public RuntimeApplication(ServletContext servletContext, ApplicationModel applicationModel, boolean production)
    {
        this.servletContext = servletContext;
        this.applicationModel = applicationModel;
        this.production = production;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }

    public void route(HttpServletRequest request, HttpServletResponse response, String rest)
    {



    }
}

