package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.action.ActionResult;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.action.JSONParameters;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.DomainServiceRepository;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Controller handling the server-side of the unified action system.
 */
@Controller
public class ActionController
{
    private final static Logger log = LoggerFactory.getLogger(ActionController.class);

    private final ApplicationService applicationService;

    private final ServletContext servletContext;

    private final ActionService actionService;

    private final RuntimeContextFactory runtimeContextFactory;

    private final ScopedContextFactory scopedContextFactory;

    private final DomainServiceRepository domainServiceRepository;


    @Autowired
    public ActionController(
        ApplicationService applicationService, ServletContext servletContext, ActionService actionService,
        RuntimeContextFactory runtimeContextFactory,
        ScopedContextFactory scopedContextFactory,
        DomainServiceRepository domainServiceRepository
    )
    {
        this.applicationService = applicationService;
        this.servletContext = servletContext;
        this.actionService = actionService;
        this.runtimeContextFactory = runtimeContextFactory;
        this.scopedContextFactory = scopedContextFactory;
        this.domainServiceRepository = domainServiceRepository;
    }


    @RequestMapping(value = "/action/{app}/{action}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> executeAction(
        @PathVariable("app") String appName,
        @PathVariable("action") String actionName,
        @RequestBody String parametersJSON,

        HttpServletRequest request
    ) throws IOException
    {

        // TODO: action security?
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            return new ResponseEntity<String>(
                "{\"ok\":false,\"error\":\"Action not found\"}",
                HttpStatus.NOT_FOUND
            );
        }

        final ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        SessionContext sessionContext = scopedContextFactory.getSessionContext(request, appName, applicationModel.getSessionContextModel());

        final ScopedContextChain scopedContextChain = new ScopedContextChain(
            Arrays.asList(
                runtimeApplication.getApplicationContext(),
                sessionContext
            ),
            applicationModel.getMetaData().getScopeMetaModel(),
            ScopeMetaModel.ACTION);

        final RuntimeContext runtimeContext = runtimeContextFactory.create(
            runtimeApplication,
            "/action/" + appName + "/" + actionName,
            request.getLocale(),
            scopedContextChain,
            domainServiceRepository.getDomainService(appName)
        );

        RuntimeContextHolder.register(runtimeContext, request);
        scopedContextFactory.initializeContext(runtimeContext.getJsEnvironment(), runtimeContext, sessionContext);

        try
        {
            final ActionResult out = actionService.execute(
                runtimeContext,
                actionName,
                new JSONParameters(
                    ActionService.parseArgs( parametersJSON)
                )
            );

            return new ResponseEntity<>(
                JSONUtil.DEFAULT_GENERATOR.forValue( out.toJSON() ),
                HttpStatus.OK
            );

        }
        catch (ActionNotFoundException e)
        {
            return new ResponseEntity<>(
                JSONUtil.error("Action not found"),
                HttpStatus.NOT_FOUND
            );
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(
                JSONUtil.error("Action " + parametersJSON + " failed"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        finally
        {
            RuntimeContextHolder.clear();
        }
    }


    @ExceptionHandler(value = ActionNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> actionNotFound(HttpServletResponse response, ActionNotFoundException e)
    {
        response.setContentType(ContentType.JSON);
        return new ResponseEntity<String>(JSONUtil.error(e), HttpStatus.NOT_FOUND);
    }
}
