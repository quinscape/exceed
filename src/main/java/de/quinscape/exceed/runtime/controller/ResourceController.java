package de.quinscape.exceed.runtime.controller;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.CachedResource;
import de.quinscape.exceed.runtime.util.MediaTypeService;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Controller
public class ResourceController
{
    private final static Logger log = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MediaTypeService mediaTypeService;

    @Autowired
    private ServletContext servletContext;


    @RequestMapping("/res/{appName}/**")
    public ResponseEntity serveResource(
        @PathVariable("appName") String appName,
        ModelMap model, HttpServletRequest request, HttpServletResponse response) throws IOException, ExecutionException
    {

        DefaultRuntimeApplication runtimeApplication = (DefaultRuntimeApplication) applicationService.getRuntimeApplication(

            appName);
        if (runtimeApplication == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Application '" + appName + "' not found");
            return null;
        }

        String resourceURI = RequestUtil.getRemainingURI(request, appName.length() + 5);

        if (resourceURI.equals("/style/" + appName + ".css"))
        {
            byte[] data = runtimeApplication.getCollectedStyles().getBytes(RequestUtil. UTF_8);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/css");
            response.setContentLength(data.length);

            ServletOutputStream os = response.getOutputStream();
            os.write(data);
            os.flush();
            return null;
        }

        String resourcePath = "/resources" + resourceURI;

        int matrixArgs = resourcePath.indexOf(';');
        if (matrixArgs > 0)
        {
            resourcePath = resourcePath.substring(0, matrixArgs);
        }

        log.debug("Serve resource {}:{}", appName, resourcePath);

        LoadingCache<String, CachedResource> resourceCache = runtimeApplication.getResourceLoader().getResourceCache();

        if (resourceCache == null)
        {
            throw new IllegalStateException("Need resource cache");
        }

        CachedResource cachedResource = resourceCache.get(resourcePath);

        String previousETag = request.getHeader("If-None-Match");
        String eTag = cachedResource.getId();

        response.setHeader("Etag", eTag);
        response.setHeader("Expires", "");
        response.setHeader("Pragma", "");
        response.setHeader("Cache-Control", "public, max-age=31536000");

        if (previousETag != null && previousETag.equals(eTag))
        {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        final String gzipETag = eTag + "-gzip";
        if (previousETag != null && previousETag.equals(gzipETag))
        {
            response.setHeader("Etag", gzipETag);
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        byte[] data = cachedResource.getData();

        String mediaType = mediaTypeService.getContentType(resourcePath);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(mediaType);
        response.setContentLength(data.length);

        ServletOutputStream os = response.getOutputStream();
        os.write(data);
        os.flush();
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
