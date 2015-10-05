package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.config.JsService;
import de.quinscape.exceed.runtime.util.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ScriptController
{
    @Autowired
    private JsService jsService;

    private static Logger log = LoggerFactory.getLogger(ScriptController.class);

    @RequestMapping("/code/{src:.+}")
    public void showApplicationView(
        @PathVariable("src") String src,
        ModelMap model, HttpServletRequest request, HttpServletResponse response) throws IOException
    {

        boolean isMainSource = src.equals("main.js");
        if (!isMainSource && !src.equals("main.js.map"))
        {
            log.debug("Error on {}", src);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        log.debug("Serve {}", src);
        String mediaType;
        byte[] data;
        if (isMainSource)
        {
            mediaType = ContentType.JAVASCRIPT;
            data = jsService.getSourceData();
        }
        else
        {
            mediaType = ContentType.PLAINTEXT;
            data = jsService.getMapData();
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType(mediaType);
        response.setContentLength(data.length);

        ServletOutputStream os = response.getOutputStream();
        os.write(data);
        os.flush();
    }
}
