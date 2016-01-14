package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Layout;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.svenson.JSON;
import org.svenson.JSONParser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class EditorController
{
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping("/editor/{app}/domain")
    @ResponseBody
    public String domainModels(
        @PathVariable("app") String appName,
        HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
            return null;
        }

        String json;

        Map<String, Object> data = new HashMap<>();

        ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        data.put("domainTypes", applicationModel.getDomainTypes());
        data.put("propertyTypes", applicationModel.getPropertyTypes());
        data.put("domainLayout", applicationModel.getDomainLayout());
        data.put("enums", applicationModel.getEnums());

        response.setContentType(ContentType.JSON);
        return JSON.defaultJSON().forValue(data);
    }

    @RequestMapping(value = "/editor/{app}/domain/layout", method = RequestMethod.POST)
    @ResponseBody
    public String saveLayout(
        @PathVariable("app") String appName,
        @RequestBody String json,
        HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
            return null;
        }

        Map<String,Object> data = JSONParser.defaultJSONParser().parse(Map.class, json);

        ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        synchronized (applicationModel)
        {
            Layout domainLayout = applicationModel.getDomainLayout();
            if (domainLayout == null)
            {
                domainLayout = new Layout();
                applicationModel.setDomainLayout(domainLayout);
            }
            domainLayout.setProperty((String) data.get("name"), data.get("layout"));
        }

        response.setContentType(ContentType.JSON);
        return "{\"ok\":true}";
    }

}
