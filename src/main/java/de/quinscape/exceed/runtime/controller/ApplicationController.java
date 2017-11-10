package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.ApplicationSecurityException;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.MappingNotFoundException;
import de.quinscape.exceed.runtime.application.StateNotFoundException;
import de.quinscape.exceed.runtime.config.WebpackConfig;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.ServiceNotReadyException;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ApplicationController
{

    public final static Logger log = LoggerFactory.getLogger(ApplicationController.class);

    public static final String BASE_TEMPLATE_RESOURCE = "/resources/template/template.html";

    private final ServletContext servletContext;

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ServletContext servletContext, ApplicationService applicationService)
    {
        this.servletContext = servletContext;
        this.applicationService = applicationService;
    }


    /**
     * Redirect a request to servlet context root to our default app.
     */
    @RequestMapping("/")
    public String redirectToDefault(
        HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try
        {
            final String defaultApp = applicationService.getDefaultApplication();
            response.sendRedirect(request.getContextPath() + "/app/" + defaultApp + "/");
            return null;
        }
        catch(ServiceNotReadyException e)
        {
            sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, request, response, null, "Application service not ready. Try again.");
            return null;
        }
    }

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

            model.addAttribute(TemplateVariables.TITLE, "\u2026");

            boolean done = runtimeApplication.route(request, response, model, inAppURI);
            if (!done)
            {
                return appName + ":" + WebpackConfig.APP_BUNDLES;
            }
        }
        catch(MappingNotFoundException e)
        {
            sendError(HttpServletResponse.SC_NOT_FOUND, request, response, e, "Mapping not found");
        }
        catch(ApplicationNotFoundException e)
        {
            sendError(HttpServletResponse.SC_NOT_FOUND, request, response, e, "Application '" + appName + "' not found");
        }
        catch(ApplicationSecurityException e)
        {
            sendError(HttpServletResponse.SC_FORBIDDEN, request, response, e, "Application security error");
        }
        catch(StateNotFoundException e)
        {
            sendError(HttpServletResponse.SC_NOT_FOUND, request, response, e, "State not found");
        }
        catch(ServiceNotReadyException e)
        {
            sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, request, response, null, "Application service not ready. Try again.");
        }
        catch(Exception e)
        {
            log.error("Error rendering application view", e);
            response.sendError(500);
        }
        return null;
    }


    private void sendError(
        int code,
        HttpServletRequest request,
        HttpServletResponse response,
        Exception e,
        String message
    ) throws IOException
    {
        if (RequestUtil.isAjaxRequest(request))
        {
            response.setContentType(ContentType.JSON);
            RequestUtil.sendJSON(response, JSONUtil.error(message + ( e != null ? ": " + e.getMessage() : "")));
        }
        else
        {
            response.sendError(code, message);
        }
    }
}
