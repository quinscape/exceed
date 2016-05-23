package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.MappingNotFoundException;
import de.quinscape.exceed.runtime.application.StateNotFoundException;
import de.quinscape.exceed.runtime.security.Roles;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ApplicationController
{
    private final static Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ScopedContextFactory scopedContextFactory;

    @Autowired
    private RuntimeContextFactory runtimeContextFactory;

    @Autowired
    private MessageHubRegistry messageHubRegistry;

    @RequestMapping("/app/{name}/**")
    public String showApplicationView(
        @PathVariable("name") String appName,
        HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException
    {
        String inAppURI = RequestUtil.getRemainingURI( request, appName.length() + 5);

        log.debug("showApplicationView: app = {} path = {}", appName, inAppURI);
        try
        {
            DefaultRuntimeApplication runtimeApplication = (DefaultRuntimeApplication) applicationService.getRuntimeApplication(servletContext, appName);
            if (runtimeApplication == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
                return null;
            }

            model.put("title", "Application View");
            model.put("appName", appName);

            runtimeApplication.route(request, response, model, inAppURI);

            AppAuthentication auth = AppAuthentication.get();
            if (auth.hasRole(Roles.EDITOR) && !RequestUtil.isAjaxRequest(request))
            {
                RuntimeContext runtimeContext = RuntimeContextHolder.get();
                String connectionId = messageHubRegistry.registerConnection(request.getSession(), runtimeContext, auth);
                model.put("connectionId", connectionId);
            }

            if (response.isCommitted())
            {
                return null;
            }
            else
            {
                return "react-base";
            }
        }
        catch(MappingNotFoundException e)
        {
            return sendNotFoundError(request, response, e, "Mapping not found");
        }
        catch(ApplicationNotFoundException e)
        {
            return sendNotFoundError(request, response, e, "Application '" + appName + "' not found");
        }
        catch(StateNotFoundException e)
        {
            return sendNotFoundError(request, response, e, "State not found");
        }
        catch(Exception e)
        {
            log.error("Error rendering application view", e);
            response.sendError(500);
            return null;
        }
        finally
        {
            RuntimeContextHolder.clear();
        }
    }


    private String sendNotFoundError(HttpServletRequest request, HttpServletResponse response, Exception e, String message) throws IOException
    {
        if (RequestUtil.isAjaxRequest(request))
        {
            response.setContentType(ContentType.JSON);
            RequestUtil.sendJSON(response, "{\"ok\":false,\"error\":" + JSON.defaultJSON().quote(message + ": " + e.getMessage()) + "}");
            return null;
        }
        else
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
            return null;
        }
    }
}
