package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.resource.DefaultResourceLoader;

import javax.servlet.ServletContext;
import java.util.Map;

public interface RuntimeApplicationFactory
{
    DefaultRuntimeApplication createRuntimeApplication(
        ServletContext servletContext,
        DefaultResourceLoader resourceLoader,
        DomainService domainService,
        ApplicationModel applicationModel,
        Map<String, ExceedDataSource> dataSources
    );
}
