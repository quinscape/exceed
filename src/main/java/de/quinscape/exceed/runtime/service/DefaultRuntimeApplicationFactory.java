package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.resource.DefaultResourceLoader;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultRuntimeApplicationFactory
    implements RuntimeApplicationFactory
{

    private final static Logger log = LoggerFactory.getLogger(DefaultRuntimeApplicationFactory.class);

    private final ApplicationContext applicationContext;

    private final ModelCompositionService modelCompositionService;

    private final ViewDataService viewDataService;

    private final ProcessService processService;

    private final RuntimeContextFactory runtimeContextFactory;

    private final ScopedContextFactory scopedContextFactory;

    private final ClientStateService clientStateService;

    private Set<ClientStateProvider> clientStateProviders;

    @Autowired
    public DefaultRuntimeApplicationFactory(
        ApplicationContext applicationContext,
        ModelCompositionService modelCompositionService,
        ClientStateService clientStateService,
        ViewDataService viewDataService,
        ProcessService processService,
        RuntimeContextFactory runtimeContextFactory,
        ScopedContextFactory scopedContextFactory
    )
    {
        this.applicationContext = applicationContext;
        this.modelCompositionService = modelCompositionService;
        this.clientStateService = clientStateService;
        this.viewDataService = viewDataService;
        this.processService = processService;
        this.runtimeContextFactory = runtimeContextFactory;
        this.scopedContextFactory = scopedContextFactory;
    }


    @Override
    public DefaultRuntimeApplication createRuntimeApplication(
        ServletContext servletContext, DefaultResourceLoader resourceLoader, DomainService domainService,
        ApplicationModel applicationModel,
        Map<String, ExceedDataSource> dataSources
    )
    {
        return new DefaultRuntimeApplication(
            viewDataService,
            modelCompositionService,
            clientStateService,
            processService,
            runtimeContextFactory,
            scopedContextFactory,
            clientStateProviders,
            servletContext,
            resourceLoader,
            domainService,
            applicationModel,
            dataSources
        );
    }


    @PostConstruct
    public void initProviders()
    {
        clientStateProviders = ClientStateService.findProviderBeans(applicationContext, ExceedAppProvider.class);
    }


}

