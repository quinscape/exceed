package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
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
import java.util.concurrent.TimeUnit;

@Controller
public class HotReloadController
{
    private final static Logger log = LoggerFactory.getLogger(HotReloadController.class);

    public final static long TIMEOUT = TimeUnit.SECONDS.toMillis(110);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ModelJSONService modelJSONService;

    private final static Charset UTF8 = Charset.forName("UTF-8");

    @RequestMapping("/reload/{name}")
    public void pollForChanges(
        @PathVariable("name") String appName,
        HttpServletRequest request, HttpServletResponse response
    ) throws IOException, InterruptedException
    {
        DefaultRuntimeApplication runtimeApplication = (DefaultRuntimeApplication) applicationService.getRuntimeApplication(servletContext, appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
            return;
        }

        // will block until a change happens or a timeout.
        Model changedModel = runtimeApplication.waitForChange(TIMEOUT);

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
        catch(IOException e)
        {
            log.debug("Error writing response", e);
            IOUtils.closeQuietly(os);
        }
    }
}
