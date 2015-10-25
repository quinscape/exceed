package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
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
    private static Logger log = LoggerFactory.getLogger(ApplicationController.class);


    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RuntimeContextFactory runtimeContextFactory;

    @RequestMapping("/app/{name}/**")
    public String showApplicationView(
        @PathVariable("name") String appName,
        HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException
    {
        String rest = request.getRequestURI().substring(request.getContextPath().length() + appName.length() + 6);
        log.debug("showApplicationView: app = {} path = {}", appName, rest);
        try
        {
            RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
            if (runtimeApplication == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
                return null;
            }

            RuntimeContext runtimeContext = runtimeContextFactory.create(request, response, model, runtimeApplication, "/" + rest);

            RuntimeContextHolder.register(runtimeContext);

            runtimeApplication.route(runtimeContext);

            if (response.isCommitted())
            {
                return null;
            }
            else
            {
                model.put("title", "Application View");
                model.put("appName", appName);
                return "react-base";
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
