package de.quinscape.exceed.runtime.config;

import de.quinscape.dss.util.Util;
import de.quinscape.exceed.runtime.controller.ActionRegistry;
import de.quinscape.exceed.runtime.security.ApplicationUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommonVariablesInterceptor
    extends HandlerInterceptorAdapter
{
    private static Logger log = LoggerFactory.getLogger(CommonVariablesInterceptor.class);


    public final static String CONTEXT_PATH = "contextPath";

    public final static String USER_NAME = "userName";

    public final static String USER_ROLES = "userRoles";

    public final static String SYSTEM_INFO = "systemInfo";

    private final ApplicationContext applicationContext;

    private final ServletContext servletContext;

    private volatile String systemInfo;


    public CommonVariablesInterceptor(ApplicationContext applicationContext, ServletContext servletContext)
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
            modelAndView.addObject(CONTEXT_PATH, request.getContextPath());

            SecurityContext context = SecurityContextHolder.getContext();

            Authentication authentication = context.getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof ApplicationUserDetails)

            {
                ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

                modelAndView.addObject("userName", userDetails.getUsername());
                modelAndView.addObject("userRoles", userDetails.getRoles());
            }
            else
            {
                modelAndView.addObject(USER_NAME, "Anonymous");
                modelAndView.addObject(USER_ROLES, "ANONYMOUS");
            }

            modelAndView.addObject("systemInfo", getSystemInfo());
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
        Map<String,Object> info = new HashMap<>();

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
