package de.quinscape.exceed.runtime.controller;

import com.google.common.util.concurrent.UncheckedExecutionException;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.StateNotFoundException;
import de.quinscape.exceed.runtime.resource.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class ExceedExceptionResolver
    extends AbstractHandlerExceptionResolver
{
    private final static Logger log = LoggerFactory.getLogger(ExceedExceptionResolver.class);


    public ExceedExceptionResolver()
    {
        setOrder(HIGHEST_PRECEDENCE);
    }


    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object o,
                                              Exception e)
    {
        log.error("ERROR: {}\n{}", o, e);

        Map<String, Object> map = new HashMap<>();


        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:'Z'"); // Quoted "Z" to indicate UTC, no
        // timezone offset
        df.setTimeZone(tz);

        map.put("error", e.getLocalizedMessage());
        map.put("timestamp", df.format(new Date()));

        final int code = httpCode(e);
        map.put("status", code);

        response.setStatus(code);

        return new ModelAndView("error", map);
    }


    private int httpCode(Exception e)
    {
        // guava cache might wrap
        if (e instanceof UncheckedExecutionException)
        {
            e = (Exception) e.getCause();
        }
        if (e instanceof ExecutionException)
        {
            e = (Exception) e.getCause();
        }

        if (e instanceof ResourceNotFoundException)
        {
            return HttpServletResponse.SC_NOT_FOUND;
        }
        else if (e instanceof ApplicationNotFoundException)
        {
            return HttpServletResponse.SC_NOT_FOUND;
        }
        else if (e instanceof StateNotFoundException)
        {
            return HttpServletResponse.SC_NOT_FOUND;
        }
        else
        {
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

    }
}
