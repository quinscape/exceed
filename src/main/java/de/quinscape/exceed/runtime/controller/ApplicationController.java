package de.quinscape.exceed.runtime.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class ApplicationController
{
    private static Logger log = LoggerFactory.getLogger(ApplicationController.class);


    @Autowired
    private ServletContext servletContext;

    @RequestMapping("/app/{name}/{rest:.*}")
    public String showApplicationView(
        @PathVariable("name") String appName,
        @PathVariable("rest") String rest,
        HttpServletRequest request, ModelMap model) throws IOException
    {
        return reactView(request, model, appName, rest, "Application View", true);
    }

    @RequestMapping("/editor/{name}/{rest:.*}")
    public String showEditor(
        @PathVariable("name") String appName,
        @PathVariable("rest") String rest,
        HttpServletRequest request, ModelMap model)
    {
        return reactView(request, model, appName, rest, "Application Editor", false);
    }

    private String reactView(HttpServletRequest request, ModelMap model, String appName, String rest, String title, boolean toEditor)
    {

        String query = request.getQueryString();
        String editUrl = request.getContextPath() + "/" + (toEditor ? "editor" : "app") +  "/" + appName + "/" + rest + (query != null ? query : "");

        servletContext.getRealPath("/exceed/views/error.jsp");

        model.put("title", title);
        model.put("editUrl", editUrl);
        model.put("editText", toEditor ? "Edit" : "Preview");
        return "react-base";
    }
}
