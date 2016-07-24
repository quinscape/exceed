package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.MappingNotFoundException;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.application.StateNotFoundException;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.security.Roles;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.template.BaseTemplate;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
public class ApplicationController
    implements TemplateVariables
{

    private final static Logger log = LoggerFactory.getLogger(ApplicationController.class);

    public static final String BASE_TEMPLATE_RESOURCE = "/resources/template/template.html";

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MessageHubRegistry messageHubRegistry;

    @RequestMapping("/app/{name}/**")
    public String showApplicationView(
        @PathVariable("name") String appName,
        ModelMap model,
        HttpServletRequest request, HttpServletResponse response) throws IOException
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

            model.addAttribute(TITLE, "\u2026");

            boolean done = runtimeApplication.route(request, response, model, inAppURI);

            AppAuthentication auth = AppAuthentication.get();
            if (auth.hasRole(Roles.EDITOR) && !RequestUtil.isAjaxRequest(request))
            {
                RuntimeContext runtimeContext = RuntimeContextHolder.get();
                String connectionId = messageHubRegistry.registerConnection(request.getSession(), runtimeContext, auth);
                model.addAttribute(CONNECTION_ID, connectionId);
            }

            if (!done)
            {
                return appName + ":base";
            }
        }
        catch(MappingNotFoundException e)
        {
            sendNotFoundError(request, response, e, "Mapping not found");
        }
        catch(ApplicationNotFoundException e)
        {
            sendNotFoundError(request, response, e, "Application '" + appName + "' not found");
        }
        catch(StateNotFoundException e)
        {
            sendNotFoundError(request, response, e, "State not found");
        }
        catch(Exception e)
        {
            log.error("Error rendering application view", e);
            response.sendError(500);
        }
        finally
        {
            RuntimeContextHolder.clear();
        }
        return null;
    }

    private void sendNotFoundError(HttpServletRequest request, HttpServletResponse response, Exception e, String message) throws IOException
    {
        if (RequestUtil.isAjaxRequest(request))
        {
            response.setContentType(ContentType.JSON);
            RequestUtil.sendJSON(response, "{\"ok\":false,\"error\":" + JSON.defaultJSON().quote(message + ": " + e.getMessage()) + "}");
        }
        else
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
    }
}
