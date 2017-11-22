package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.staging.DataSourceModel;
import de.quinscape.exceed.model.staging.StageModel;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ContextUpdate;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.DataProviderPreparationException;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.process.ProcessExecutionState;
import de.quinscape.exceed.runtime.process.TransitionInput;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.schema.SchemaService;
import de.quinscape.exceed.runtime.scope.ApplicationContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.scope.UserContext;
import de.quinscape.exceed.runtime.scope.ViewContext;
import de.quinscape.exceed.runtime.service.ProcessService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.LocationUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.util.Util;
import de.quinscape.exceed.runtime.view.ComponentData;
import de.quinscape.exceed.runtime.view.ViewData;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.svenson.JSONParseException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    implements RuntimeApplication
{
    public static final String PREVIEW_HEADER_NAME = "X-ceed-Preview";

    public static final String UPDATE_HEADER_NAME = "X-ceed-Update";

    public static final String UPDATE_ID_PARAM = "_id";

    public static final String UPDATE_VARS_PARAM = "_vars";

    public static final String TRANSITION_PARAM = "_trans";

    private final static Logger log = LoggerFactory.getLogger(DefaultRuntimeApplication.class);

    private static final String SYSTEM_CONTEXT_PATH = "/sys";


    public static final String STATE_ID_PARAMETER = "stateId";

    private static final String CONTEXT_UPDATE_PARAM = "contextUpdate";

    private final ApplicationModel applicationModel;

    private final ViewDataService viewDataService;

    private final ModelCompositionService modelCompositionService;

    private final ResourceLoader resourceLoader;

    private final DomainService domainService;

    private final ProcessService processService;

    private final ApplicationContext applicationContext;

    private final ScopedContextFactory scopedContextFactory;

    private final ClientStateService clientStateService;

    private final RuntimeContextFactory runtimeContextFactory;

    private final Set<ClientStateProvider> clientStateProviders;


    public DefaultRuntimeApplication(
        ViewDataService viewDataService,
        ModelCompositionService modelCompositionService,
        ClientStateService clientStateService,
        ProcessService processService,
        RuntimeContextFactory runtimeContextFactory,
        ScopedContextFactory scopedContextFactory,
        Set<ClientStateProvider> clientStateProviders,
        ServletContext servletContext,
        ResourceLoader resourceLoader,
        DomainService domainService,
        ApplicationModel applicationModel,
        Map<String, ExceedDataSource> dataSources
    )
    {
        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }


        this.processService = processService;
        this.scopedContextFactory = scopedContextFactory;
        this.runtimeContextFactory = runtimeContextFactory;
        this.clientStateProviders = clientStateProviders;

        this.viewDataService = viewDataService;
        this.modelCompositionService = modelCompositionService;
        this.resourceLoader = resourceLoader;
        this.clientStateService = clientStateService;
        this.applicationModel = applicationModel;

        //boolean production = ApplicationStatus.from(state) == ApplicationStatus.PRODUCTION;

        this.domainService = domainService;

        RuntimeContext systemContext = createSystemContext();
        RuntimeContextHolder.register(systemContext, null);

        applicationContext = scopedContextFactory.createApplicationContext(applicationModel.getConfigModel().getApplicationContextModel(), applicationModel.getName());

        domainService.init(this, dataSources);

        synchronizeDomainTypeSchemata(systemContext);
        ensureBaseRoles(systemContext);
        scopedContextFactory.initializeContext(applicationModel.getMetaData().getJsEnvironment(), systemContext, applicationContext);
    }

    private void ensureBaseRoles(
        RuntimeContext systemContext
    )
    {
        final Map<String, Set<String>> defaultUsers = applicationModel.getConfigModel().getDefaultUsers();

        final List<DomainObject> domainObjects = AppAuthentication.queryUsers(
            systemContext,
            DSL.field("login").in(defaultUsers.keySet())
        );

        BCryptPasswordEncoder encoder = null;

        for (Map.Entry<String, Set<String>> e : defaultUsers.entrySet())
        {
            final String name = e.getKey();
            final Set<String> roles = e.getValue();

            DomainObject user = findUser(domainObjects, name);
            if (user == null)
            {
                if (encoder == null)
                {
                    encoder = new BCryptPasswordEncoder();
                }

                createDefaultUser(systemContext, encoder, name, roles);
            }
        }
    }


    private void createDefaultUser(
        RuntimeContext systemContext,
        BCryptPasswordEncoder encoder,
        String name,
        Set<String> roles
    )
    {
        DomainObject user;
        log.info("Creating default user '" + name + "'");

        final Timestamp now = Timestamp.from(Instant.now());

        user = domainService.create(systemContext, AppAuthentication.USER_TYPE, UUID.randomUUID().toString());
        user.setProperty("login", name);
        user.setProperty("password", encoder.encode(name));
        user.setProperty("roles", Util.join(roles,","));
        user.setProperty("created", now);
        user.setProperty("last_login", now);
        user.setProperty("disabled", false);
        user.insert(systemContext);
    }


    private DomainObject findUser(List<DomainObject> domainObjects, String name)
    {
        for (DomainObject user : domainObjects)
        {
            if (user.getProperty("login").equals(name))
            {
                return user;
            }

        }
        return null;
    }


    public RuntimeContext createSystemContext()
    {
        return this.runtimeContextFactory.create(
            this,
                SYSTEM_CONTEXT_PATH,
                Locale.forLanguageTag("en-US"),
                new ScopedContextChain(
                    Collections.singletonList(
                        applicationContext
                    ),
                    applicationModel.getMetaData().getScopeMetaModel(),
                    ScopeMetaModel.SYSTEM
                ),
                this.domainService
            );
    }


    private void synchronizeDomainTypeSchemata(
        RuntimeContext systemContext
    )
    {
        final Map<String, List<DomainType>> map = mapDomainTypesByDataSource(systemContext);

        for (Map.Entry<String, List<DomainType>> entry : map.entrySet())
        {
            final String dataSourceName = entry.getKey();
            final ExceedDataSource dataSource = systemContext.getDomainService().getDataSource(dataSourceName);

            final SchemaService schemaService = dataSource.getStorageConfiguration().getSchemaService();
            if (schemaService != null)
            {
                schemaService.synchronizeSchema(systemContext, dataSource, entry.getValue());
            }
        }
    }


    private Map<String, List<DomainType>> mapDomainTypesByDataSource(RuntimeContext runtimeContext)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        final Map<String,List<DomainType>> byDataSource = new HashMap<>();

        final StageModel mergedStageModel = applicationModel.getMetaData().getMergedStageModel();
        final Map<String, DataSourceModel> dataSourceModels = mergedStageModel.getDataSourceModels();

        for (DomainType type : runtimeContext.getDomainService().getDomainTypes().values())
        {
            if (type instanceof QueryTypeModel || type.isSystem())
            {
                continue;
            }

            final String dataSourceName;
            if (type.getDataSourceName() != null)
            {
                dataSourceName = type.getDataSourceName();
            }
            else
            {
                dataSourceName = applicationModel.getConfigModel().getDefaultDataSource();
            }

            final DataSourceModel dataSourceModel = dataSourceModels.get(dataSourceName);

            if (dataSourceModel == null)
            {
                throw new IllegalStateException("Could not find data source with name '" + dataSourceName + "' in " + mergedStageModel);
            }
            
            List<DomainType> domainTypes = byDataSource.get(dataSourceName);
            if (domainTypes == null)
            {
                domainTypes = new ArrayList<>();
                byDataSource.put(dataSourceName, domainTypes);
            }
            domainTypes.add(type);
        }
        return byDataSource;
    }


    @Override
    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public boolean route(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, String inAppURI) throws Exception
    {
        RoutingResult result = applicationModel.getRoutingTable().resolve(inAppURI);
        Mapping mapping = result.getMapping();
        final Map<String, String> variables = result.getVariables();
        final String template = result.getTemplate();
        String viewName = mapping.getViewName();
        String processName = mapping.getProcessName();

        return processView(request, response, model, inAppURI, variables, template, viewName, processName);
    }


    public boolean  processView(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, Object> model,
            String inAppURI,
            Map<String, String> variables,
            String template,
            final String viewName,
            final String processName
        ) throws IOException, ParseException
    {
        final AppAuthentication auth = AppAuthentication.get();

        UserContext userContext= null;
        SessionContext sessionContext = null;
        RuntimeContext runtimeContext = null;
        try
        {
            sessionContext = scopedContextFactory.getSessionContext(
                request,
                applicationModel.getName(),
                applicationModel.getConfigModel().getSessionContextModel()
            );


            userContext = scopedContextFactory.createUserContext(
                applicationModel.getConfigModel().getUserContextModel(),
                auth.getUserName()
            );

            runtimeContext = runtimeContextFactory.create(
                this, inAppURI,
                request.getLocale(),
                new ScopedContextChain(
                    Arrays.asList(
                        applicationContext,
                        sessionContext,
                        userContext
                    )
                    , applicationModel.getMetaData().getScopeMetaModel(), null),
                domainService);


            final String appAuth = applicationModel.getConfigModel().getAuthSchema();
            if (!auth.isAnonymous() && !auth.isAdmin() && !auth.canAccess(appAuth))
            {
                throw new ApplicationSecurityException(
                    "Authentication schema does not match the application's authSchema. " +
                    "User was authenticated for another application. " +
                    "Make applications share an authSchema if shared access desired: " +
                    "auth = " + auth + ", app auth = " + appAuth
                );
            }

            RuntimeContextHolder.register(runtimeContext, request);
            scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, userContext);
            scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, sessionContext);


            runtimeContext.setVariables(addRequestParameters(request, new HashMap<>(variables)));
            runtimeContext.setRoutingTemplate(template);

            View view;
            final boolean isAjaxRequest = RequestUtil.isAjaxRequest(request);
            final boolean isPreview = isAjaxRequest && isPreviewRequest(request);
            final boolean isComponentUpdate = isAjaxRequest && isComponentUpdate(request);

            TransitionInput transitionData = null;

            ProcessExecutionState state = null;
            if (processName != null)
            {
                Process process = applicationModel.getProcess(processName);
                String stateId = (String) runtimeContext.getLocationParams().get(STATE_ID_PARAMETER);

                boolean redirect;
                if (stateId == null)
                {
                    log.debug("Start process {}", process.getName());

                    state = processService.start(runtimeContext, process);
                    state.register(request.getSession());
                    redirect = true;
                }
                else
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
                        final String json = RequestUtil.readRequestBody(request);

                        transitionData = TransitionInput.parse(runtimeContext, initialState.getExecution().getProcessName(), initialState.getCurrentState(), json);
                        state = processService.resume(runtimeContext, initialState, transition, transitionData);
                        state.register(request.getSession());

                        runtimeContext.setVariable(STATE_ID_PARAMETER, state.getId());

                        redirect = !isAjaxRequest;
                    }
                    else
                    {
                        log.debug("(Re)render state {}", stateId);
                        state = initialState;

                        runtimeContext.getScopedContextChain().update(state.getScopedContext(), Process.getProcessStateName(processName, state.getCurrentState()));

                        redirect = false;
                    }
                }

                if (redirect)
                {
                    Map<String, Object> params = runtimeContext.getLocationParams();
                    // request ends here anyway, can change in-place
                    params.put("stateId", state.getId());
                    params.remove(TRANSITION_PARAM);

                    response.sendRedirect(request.getContextPath() + "/app/" + applicationModel.getName() + LocationUtil.evaluateURI(template, params));
                    return true;
                }

                ProcessState processState = process.getStates().get(state.getCurrentState());
                if (processState == null)
                {
                    throw new IllegalStateException("Process state '" + state.getCurrentState() + "' does not exist " +
                        "in " + process);
                }

                String processViewName = Process.getProcessStateName(state.getExecution().getProcessName(), state.getCurrentState());
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
                return true;
            }

            if (view == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "View " + viewName + " not found" +
                    ".");
                return true;
            }

            // process service will have already initialized a view context unless a start state is redirect-after-posted to.
            if (runtimeContext.getScopedContextChain().getViewContext() == null)
            {
                final ViewContext viewContext = scopedContextFactory.createViewContext(view);
                scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, viewContext);
                runtimeContext.getScopedContextChain().update(viewContext, view.getName());
            }

            Map<String,Object> postData = Collections.emptyMap();
            if ( transitionData == null && (isPreview || isComponentUpdate))
            {
                postData = (Map<String, Object>) JSONUtil.DEFAULT_PARSER.parse(Map.class, RequestUtil.readRequestBody(request));
            }


            if (isAjaxRequest)
            {
                if (isPreview)
                {
                    Map<String,String> changes;
                    if (transitionData != null)
                    {
                        changes = transitionData.getChangedViewModels();
                    }
                    else
                    {
                        changes = (Map<String, String>) postData.get("changedViewModels");
                    }

                    // check if the current view is in the changed view set
                    String viewJSON = changes.get(view.getName());
                    if (viewJSON != null)
                    {
                        // yes -> use the preview view instead of the stored view
                        
                        View previewView = modelCompositionService.createPreviewViewModel(view.getResource(), viewJSON);
                        if (!Objects.equals(view.getName(), previewView.getName()))
                        {
                            throw new IllegalStateException(
                                "Cannot preview different view. view = " + view + ", preview = " + previewView);
                        }
                        previewView.setProcessName(view.getProcessName());
                        modelCompositionService.postprocessView(runtimeContext.getApplicationModel(), previewView);

                        List<ComponentError> errors = new ArrayList<>();

                        for (Map.Entry<String, ComponentModel> entry : previewView.getContent().entrySet())
                        {
                            collectErrors(errors, entry.getKey(), entry.getValue(), 0);
                        }
                        
                        if (errors.size() > 0)
                        {
                            RequestUtil.sendJSON(response, JSONUtil.error("Preview error", errors));
                            return true;
                        }
                        view = previewView;
                    }
                }
            }

            if (isAjaxRequest && isComponentUpdate(request))
            {
                updateComponentVars(runtimeContext, request, response, state, view, postData);
                return true;
            }

            final String viewDataJSON;
            try
            {
                viewDataJSON = processView(request, runtimeContext, view, state);
            }
            catch (DataProviderPreparationException e)
            {
                if (isPreview)
                {
                    int index = ComponentUtil.findFlatIndex(view, e.getId());
                    model.put("previewErrors", Collections.singletonList(new ProviderError(e.getCause().getMessage(),
                        index)));
                    RequestUtil.sendJSON(response, JSONUtil.DEFAULT_GENERATOR.forValue(model));
                    return true;
                }
                else
                {
                    throw new ExceedRuntimeException("Error providing data to view", e);
                }
            }


            if (!isAjaxRequest)
            {
                model.put(TemplateVariables.VIEW_DATA, viewDataJSON);
                return false;
            }
            else
            {

                RequestUtil.sendJSON(response, viewDataJSON);
                return true;
            }
        }
        finally
        {
            if (runtimeContext != null)
            {
                scopedContextFactory.updateUserContext(runtimeContext, userContext);
                scopedContextFactory.updateSessionContext(request, applicationModel.getName(), sessionContext);
                scopedContextFactory.updateApplicationContext(runtimeContext, applicationModel.getName(), applicationContext);
            }
        }
    }


    public String processView(HttpServletRequest request, RuntimeContext runtimeContext, View view, ProcessExecutionState state) throws IOException
    {
        runtimeContext.setView(view);

        ViewData viewData = viewDataService.prepareView(runtimeContext, view, state);
        return clientStateService.getClientStateJSON(request, runtimeContext, viewData, clientStateProviders);
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



    private void updateComponentVars(RuntimeContext runtimeContext, HttpServletRequest request, HttpServletResponse response, ProcessExecutionState state, View view, Map<String, Object> postData) throws IOException, ParseException

    {
        String componentId = (String) postData.get(UPDATE_ID_PARAM);
        Map<String,Object> vars = (Map<String, Object>) postData.get(UPDATE_VARS_PARAM);
        Map<String,Object> contextUpdates = (Map<String, Object>) postData.get(CONTEXT_UPDATE_PARAM);

        if (componentId == null)
        {
            throw new IllegalStateException("componentId can't be null");
        }

        if (vars == null)
        {
            throw new IllegalStateException("vars can't be null");
        }


        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        Process process = state != null ? applicationModel.getProcess(state.getExecution().getProcessName()) : null;
        

        if (contextUpdates != null)
        {
            ContextUpdate.convertToJava(runtimeContext, process,  view, contextUpdates);
            runtimeContext.getScopedContextChain().update(contextUpdates);
        }

        ComponentModel componentModel = view.find((m) -> {
            ExpressionValue value = m.getAttribute(DomainType.ID_PROPERTY);
            return value != null && componentId.equals(value.getValue());
        });

        if (componentModel == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Component '" + componentId + "' not found");
            return;
        }

        ComponentData componentData = viewDataService.prepareComponent(runtimeContext, view, componentModel,
            vars, state);

        RequestUtil.sendJSON(response, domainService.toJSON(componentData));
    }


    private Map parseVars(String varsJSON)
    {
        try
        {
            return JSONUtil.DEFAULT_PARSER.parse(Map.class, varsJSON);
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

    private static boolean isComponentUpdate(HttpServletRequest request)
    {
         return "true".equals(request.getHeader(UPDATE_HEADER_NAME));
    }

    private int collectErrors(List<ComponentError> errors, String contentName, ComponentModel model, int id)
    {
        Attributes attrs = model.getAttrs();
        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                ExpressionValue attributeValue = attrs.getAttribute(name);
                if (attributeValue.getType() == ExpressionValueType.EXPRESSION_ERROR)
                {
                    errors.add(new ComponentError(contentName, attributeValue.getValue(), attributeValue.getExpressionError(), id, name));
                }
            }
        }

        id++;

        List<ComponentModel> kids = model.getKids();
        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                id = collectErrors(errors, contentName, kid, id);
            }
        }

        return id;
    }




    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }

    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

}

