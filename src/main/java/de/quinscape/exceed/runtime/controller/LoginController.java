package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.config.WebpackConfig;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@Controller
public class LoginController
{

    private final ApplicationService applicationService;

    private final ServletContext servletContext;


    @Autowired
    public LoginController(ApplicationService applicationService, ServletContext servletContext)
    {
        this.applicationService = applicationService;
        this.servletContext = servletContext;
    }


    @RequestMapping("/login")
    public String showLogin(
        HttpServletRequest request,
        HttpServletResponse response,
        ModelMap model)
    {
        model.addAttribute(TemplateVariables.TITLE, "Login");

        final String defaultApp = applicationService.getDefaultApplication();
        final DefaultRuntimeApplication runtimeApplication = (DefaultRuntimeApplication) applicationService.getRuntimeApplication(servletContext, defaultApp);

        try
        {
            runtimeApplication.processView(request, response, model, "/login", Collections.emptyMap(), "/login", "Login", null);
            return  defaultApp + ":" + WebpackConfig.APP_BUNDLES;
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException("Error preparing login", e);
        }
    }
}
