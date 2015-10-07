package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.RuntimeContext;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class RuntimeContextFactory
{
    public RuntimeContext create(HttpServletRequest request, HttpServletResponse response, ModelMap model, String path)
    {
        return new RuntimeContext(request, response, model, path);
    }
}
