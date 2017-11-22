package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.service.ApplicationService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

@Controller
public class HotReloadController
{
    private final static Logger log = LoggerFactory.getLogger(HotReloadController.class);


    private final ApplicationService applicationService;

    private final ServletContext servletContext;

    private final ModelJSONService modelJSONService;

    private final static Charset UTF8 = Charset.forName("UTF-8");


    @Autowired
    public HotReloadController(
        ApplicationService applicationService, ServletContext servletContext, ModelJSONService modelJSONService
    )
    {
        this.applicationService = applicationService;
        this.servletContext = servletContext;
        this.modelJSONService = modelJSONService;
    }


    @RequestMapping("/reload/{name}")
    public void pollForChanges(
        @PathVariable("name") String appName,
        HttpServletRequest request, HttpServletResponse response
    ) throws IOException, InterruptedException
    {
        Model changedModel = applicationService.waitForChange(appName);
        // will block until a change happens or a timeout.

        String json = modelJSONService.toJSON(changedModel);
        log.debug("Sending change response: {}", json);
        byte[] data = json.getBytes(UTF8);


        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setContentLength(data.length);

        ServletOutputStream os = response.getOutputStream();
        try
        {
            os.write(data);
            os.flush();
        }
        catch (IOException e)
        {
            log.debug("Error writing response", e);
            IOUtils.closeQuietly(os);
        }
    }
}
