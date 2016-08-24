package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.DomainEditorViews;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.svenson.JSONParser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
public class EditorController
{
    private final static Logger log = LoggerFactory.getLogger(EditorController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;



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

        Map<String, Object> data = JSONParser.defaultJSONParser().parse(Map.class, json);

        ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        synchronized (applicationModel)
        {
            DomainEditorViews domainLayout = applicationModel.getMetaData().getDomainEditorViews();
            if (domainLayout == null)
            {
                domainLayout = new DomainEditorViews();
                applicationModel.getMetaData().setDomainEditorViews(domainLayout);
            }
            //domainLayout.setProperty((String) data.get("name"), data.get("layout"));
        }

        response.setContentType(ContentType.JSON);
        return "{\"ok\":true}";
    }

}
