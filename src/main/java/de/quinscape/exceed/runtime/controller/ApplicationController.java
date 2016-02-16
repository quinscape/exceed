package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.security.Roles;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

@Controller
public class ApplicationController
{
    private static Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationService applicationService;

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
            RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
            if (runtimeApplication == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
                return null;
            }

            RuntimeContext runtimeContext = runtimeContextFactory.create(request, response, model, runtimeApplication, inAppURI);

            RuntimeContextHolder.register(runtimeContext);

            model.put("title", "Application View");
            model.put("appName", appName);

            AppAuthentication auth = AppAuthentication.get();
            if (auth.hasRole(Roles.EDITOR))
            {
                String connectionId = messageHubRegistry.registerConnection(request.getSession(), runtimeApplication, auth);
                model.put("connectionId", connectionId);
            }

            runtimeApplication.route(runtimeContext);

            if (response.isCommitted())
            {
                return null;
            }
            else
            {
                return "react-base";
            }
        }
        catch(ApplicationNotFoundException e)
        {
            if (RequestUtil.isAjaxRequest(request))
            {
                response.setContentType(ContentType.JSON);
                RequestUtil.sendJSON(response, "{\"ok\":false,\"error\":" + JSON.defaultJSON().quote(e.getMessage()) + "}");
                return null;
            }
            else
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
                return null;
            }
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


}
