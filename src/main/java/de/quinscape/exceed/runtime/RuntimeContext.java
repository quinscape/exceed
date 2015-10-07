package de.quinscape.exceed.runtime;

import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RuntimeContext
{
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ModelMap model;
    private final String path;

    public RuntimeContext(HttpServletRequest request, HttpServletResponse response, ModelMap model, String path)
    {
        this.request = request;
        this.response = response;
        this.model = model;
        this.path = path;
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public HttpServletResponse getResponse()
    {
        return response;
    }

    public ModelMap getModel()
    {
        return model;
    }

    public String getPath()
    {
        return path;
    }
}
