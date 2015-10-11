package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.MediaTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ResourceController
{
    private static Logger log = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MediaTypeService mediaTypeService;

    @Autowired
    private ServletContext servletContext;


    @RequestMapping("/res/{appName}/**")
    public void serveResource(
        @PathVariable("appName") String appName,
        ModelMap model, HttpServletRequest request, HttpServletResponse response) throws IOException
    {

        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
            return;
        }


        String resourcePath = "/resources/" + request.getRequestURI().substring(request.getContextPath().length() + appName.length() + 6);

        log.debug("Serve resource {} (app = {})", resourcePath, appName);

        ResourceLocation resourceLocation = runtimeApplication.getResourceLoader().getResourceLocation(resourcePath);

        if (resourceLocation == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource '" + resourcePath + "' not found");
            return;

        }

        AppResource resource = resourceLocation.getHighestPriorityResource();
        byte[] data = resource.read();

        String mediaType = mediaTypeService.getContentType(resourcePath);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(mediaType);
        response.setContentLength(data.length);

        ServletOutputStream os = response.getOutputStream();
        os.write(data);
        os.flush();
    }
}
