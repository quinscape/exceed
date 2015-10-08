package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.config.JsService;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.ContentType;
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
import java.nio.charset.Charset;

@Controller
public class StyleController
{
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    private Charset utf8 = Charset.forName("UTF-8");

    private static Logger log = LoggerFactory.getLogger(StyleController.class);

    @RequestMapping("/style/{appName}.css")
    public void showApplicationView(
        @PathVariable("appName") String appName,
        ModelMap model, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName +"' not found");
            return;
        }

        byte[] data = runtimeApplication.getCollectedStyles().getBytes(utf8);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/css");
        response.setContentLength(data.length);

        ServletOutputStream os = response.getOutputStream();
        os.write(data);
        os.flush();
    }
}
