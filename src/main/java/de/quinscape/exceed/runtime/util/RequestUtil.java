package de.quinscape.exceed.runtime.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class RequestUtil
{
    public static final Charset UTF_8 = Charset.forName("UTF-8");


    public static boolean isAjaxRequest(HttpServletRequest request)
    {
        return request != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }


    public static void sendJSON(HttpServletResponse response, String json) throws IOException
    {
        byte[] data = json.getBytes(UTF_8);

        response.setContentType(ContentType.JSON);
        response.setContentLength(data.length);
        PrintWriter pw = response.getWriter();
        IOUtils.write(data, pw, UTF_8);
        pw.flush();
    }


    public static String readRequestBody(HttpServletRequest request) throws IOException
    {
        String method = request.getMethod();
        if (!method.equals("POST"))
        {
            throw new IllegalStateException("Request must be POST method:"  + method);
        }
        return IOUtils.toString(request.getInputStream(), UTF_8);
    }


    public static String getRemainingURI(HttpServletRequest request, int offset)
    {
        int startOfRest = request.getContextPath().length() + offset;
        String requestURI = request.getRequestURI();
        return requestURI.length() >= startOfRest ? requestURI.substring( startOfRest ) : "/";
    }
}
