package de.quinscape.exceed.runtime.config;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommonVariablesInterceptor
    extends HandlerInterceptorAdapter
{
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView
        modelAndView) throws Exception
    {
        if (modelAndView != null)
        {
            modelAndView.addObject("contextPath", request.getContextPath());
        }
    }
}
