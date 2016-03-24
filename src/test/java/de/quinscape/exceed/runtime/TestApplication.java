package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.config.DefaultPropertyConverters;
import de.quinscape.exceed.runtime.datalist.DataListService;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;

public class TestApplication
    implements RuntimeApplication
{
    private final MockServletContext servletContext;
    private final ApplicationModel applicationModel;

    private final DomainService domainService;


    public TestApplication(ApplicationModel applicationModel, DomainService domainService)
    {
        this.applicationModel = applicationModel;
        this.domainService = domainService;
        servletContext = new MockServletContext();

        this.domainService.init(this, "test");
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


    @Override
    public DomainService getDomainService()
    {
        return domainService;
    }
}
