package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.service.ApplicationService;
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
    implements TemplateVariables
{

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping("/login")
    public String showLogin(
        HttpServletRequest request,
        HttpServletResponse response,
        ModelMap model)
    {
        model.addAttribute(TITLE, "Login");
        final String contextPath = request.getContextPath();

        final String defaultApp = applicationService.getDefaultApplication();
        final DefaultRuntimeApplication runtimeApplication = (DefaultRuntimeApplication) applicationService.getRuntimeApplication(servletContext, defaultApp);

        try
        {
            runtimeApplication.processView(request, response, model, "/login", Collections.emptyMap(), "/login", "Login", null);
            return  defaultApp + ":base";
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException("Error preparing login", e);
        }
    }
}
