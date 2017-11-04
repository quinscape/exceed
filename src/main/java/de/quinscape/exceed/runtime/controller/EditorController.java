package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.config.WebpackConfig;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.scope.UserContext;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.DomainServiceRepository;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.view.ViewData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Controller
public class EditorController
    implements ApplicationContextAware
{
    private final static Logger log = LoggerFactory.getLogger(EditorController.class);

    private static final View EMPTY_VIEW = new View();
    static
    {
        EMPTY_VIEW.setName("__empty__");
    }

    private final ApplicationService applicationService;

    private final ServletContext servletContext;

    private final ScopedContextFactory scopedContextFactory;

    private final RuntimeContextFactory runtimeContextFactory;

    private final DomainServiceRepository domainServiceRepository;

    private final ClientStateService clientStateService;

    private final ModelCompositionService modelCompositionService;

    private Set<ClientStateProvider> providers;

    private ApplicationContext applicationContext;


    @Autowired
    public EditorController(ApplicationService applicationService, ServletContext servletContext, ScopedContextFactory scopedContextFactory, RuntimeContextFactory runtimeContextFactory, DomainServiceRepository domainServiceRepository, ClientStateService clientStateService, ModelCompositionService modelCompositionService)
    {
        this.applicationService = applicationService;
        this.servletContext = servletContext;
        this.scopedContextFactory = scopedContextFactory;
        this.runtimeContextFactory = runtimeContextFactory;
        this.domainServiceRepository = domainServiceRepository;
        this.clientStateService = clientStateService;
        this.modelCompositionService = modelCompositionService;
    }


    @RequestMapping(value = "/editor/{app}/**", method = RequestMethod.GET)
    public String showEditorView(
        @PathVariable("app") String appName,
        ModelMap model,
        HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
            return null;
        }


        final ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        SessionContext sessionContext = scopedContextFactory.getSessionContext(request, appName, applicationModel.getConfigModel().getSessionContextModel());



        final UserContext userContext = scopedContextFactory.createUserContext(
            applicationModel.getConfigModel().getUserContextModel(),
            AppAuthentication.get().getUserName()
        );

        final ScopedContextChain scopedContextChain = new ScopedContextChain(
            Arrays.asList(
                runtimeApplication.getApplicationContext(),
                sessionContext
            ),
            applicationModel.getMetaData().getScopeMetaModel(),
            ScopeMetaModel.ACTION);

        RuntimeContext runtimeContext = runtimeContextFactory.create(
            runtimeApplication,
            "/editor/" + appName,
            request.getLocale(),
            scopedContextChain,
            domainServiceRepository.getDomainService(appName)
        );

        RuntimeContextHolder.register(runtimeContext, request);
        scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, userContext);
        scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, sessionContext);

        RuntimeContextHolder.register(runtimeContext, request);

        final ViewData viewData = new ViewData();

        final StaticFunctionReferences staticFunctionReferences = applicationModel
            .getMetaData().getStaticFunctionReferences();

        // register all editor module references
        staticFunctionReferences.getEditorTranslations().forEach(viewData::registerTranslation);

        runtimeContext.setView(EMPTY_VIEW);

        final String clientStateJSON = clientStateService.getClientStateJSON(request, runtimeContext, viewData, providers);

        //log.info("STATE: {}", clientStateJSON);

        model.addAttribute(TemplateVariables.TITLE, runtimeContext.getTranslator().translate(runtimeContext, "Exceed Editor"));
        model.put(TemplateVariables.VIEW_DATA, clientStateJSON);

        return appName + ":" + WebpackConfig.EDITOR_BUNDLES;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }


    @PostConstruct
    public void initProviders()
    {
        providers = ClientStateService.findProviderBeans(applicationContext, ExceedEditorProvider.class);

        log.info("Client state providers: {}", providers);
    }
}
