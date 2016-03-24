package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.controller.ActionRegistry;
import de.quinscape.exceed.runtime.security.ApplicationUserDetails;
import de.quinscape.exceed.runtime.service.websocket.MessageHubRegistry;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommonVariablesInterceptor
    extends HandlerInterceptorAdapter
{
    private final static Logger log = LoggerFactory.getLogger(CommonVariablesInterceptor.class);

    public final static String CONTEXT_PATH = "contextPath";

    public final static String USER_NAME = "userName";

    public final static String USER_ROLES = "userRoles";

    public final static String SYSTEM_INFO = "systemInfo";

    public final static String CONNECTION_ID = "connectionId";

    private final ApplicationContext applicationContext;

    private final ServletContext servletContext;


    private volatile String systemInfo;


    public CommonVariablesInterceptor(ApplicationContext applicationContext,
                                      ServletContext servletContext
    )
    {
        if (applicationContext == null)
        {
            throw new IllegalArgumentException("applicationContext can't be null");
        }

        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }

        this.applicationContext = applicationContext;
        this.servletContext = servletContext;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView
        modelAndView) throws Exception
    {
        if (modelAndView != null)
        {
            AppAuthentication auth = AppAuthentication.get();
            modelAndView.addObject(CONTEXT_PATH, request.getContextPath());
            modelAndView.addObject(USER_NAME, auth.getUserName());
            modelAndView.addObject(USER_ROLES, auth.getRoles());
            modelAndView.addObject(SYSTEM_INFO, getSystemInfo());
        }
    }


    public String getSystemInfo()
    {
        if (systemInfo == null)
        {
            synchronized (this)
            {
                if (systemInfo == null)
                {
                    systemInfo = JSON.defaultJSON().forValue(createSystemInfo());

                    log.info("System-Info: {}", systemInfo);
                }
            }
        }

        return systemInfo;
    }


    private Map<String, Object> createSystemInfo()
    {
        Map<String, Object> info = new HashMap<>();

        info.put("actions", getServerSideActionsInSystem());
        info.put("contextPath", servletContext.getContextPath());

        return info;
    }


    private Set<String> getServerSideActionsInSystem()
    {
        ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);
        if (actionRegistry == null)
        {
            throw new IllegalStateException("No action registry");
        }
        return actionRegistry.getActionNames();
    }
}
