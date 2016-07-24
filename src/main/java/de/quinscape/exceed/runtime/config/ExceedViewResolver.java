package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.controller.TemplateVariables;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.template.BaseTemplate;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExceedViewResolver
    implements ViewResolver
{

    private final static Logger log = LoggerFactory.getLogger(ExceedViewResolver.class);

    private static final String TEMPLATE_RESOURCE = "/resources/template/template.html";

    private final ApplicationContext applicationContext;

    private volatile ApplicationService applicationService;
    private volatile ActionService actionService;

    private final ServletContext servletContext;

    private ConcurrentMap<String, ExceedView> applicationViews = new ConcurrentHashMap<>();


    public ExceedViewResolver(ApplicationContext applicationContext, ServletContext servletContext)
    {
        this.applicationContext = applicationContext;
        this.servletContext = servletContext;
    }
    private String systemInfo;

    public String getSystemInfo()
    {
        if (systemInfo == null)
        {
            synchronized (this)
            {
                if (systemInfo == null)
                {
                    systemInfo = JSON.defaultJSON().forValue(createSystemInfo());
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
        return getActionService().getActionNames();
    }

    private ApplicationService getApplicationService()
    {
        if (applicationService == null)
        {
            synchronized (this)
            {
                if (applicationService == null)
                {
                    applicationService = applicationContext.getBean(ApplicationService.class);
                }
            }
        }

        return applicationService;
    }

    private ActionService getActionService()
    {
        if (actionService == null)
        {
            synchronized (this)
            {
                if (actionService == null)
                {
                    actionService = applicationContext.getBean(ActionService.class);
                }
            }
        }
        return actionService;
    }


    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception
    {
        String appName = resolveApp(viewName);


        final RuntimeApplication runtimeApplication = getApplicationService().getRuntimeApplication(servletContext,
            appName);

        ExceedView holder = new ExceedView(runtimeApplication, getSystemInfo(), servletContext);
        final ExceedView existing = applicationViews.putIfAbsent(appName, holder);
        if (existing == null)
        {
            // register for change events
            final TemplateChangeListener listener = new TemplateChangeListener(appName);
            for (ResourceRoot resourceRoot : runtimeApplication.getResourceLoader().getExtensions())
            {
                final ResourceWatcher watcher = resourceRoot.getResourceWatcher();
                if (watcher != null)
                {
                    watcher.register(listener);
                }
            }
        }
        else
        {
            holder = existing;
        }

        return holder;
    }


    private String resolveApp(String viewName)
    {
        int pos = viewName.indexOf(":");
        if (pos >= 0)
        {
            return viewName.substring(0, pos);
        }
        return getApplicationService().getDefaultApplication();
    }


    private static class ExceedView
        implements View
    {

        private volatile BaseTemplate template;

        private final RuntimeApplication runtimeApplication;

        private final String systemInfo;

        private final ServletContext servletContext;


        private ExceedView(RuntimeApplication runtimeApplication, String systemInfo, ServletContext servletContext)
        {
            this.runtimeApplication = runtimeApplication;
            this.systemInfo = systemInfo;
            this.servletContext = servletContext;
        }


        @Override
        public String getContentType()
        {
            return ContentType.HTML;
        }


        @Override
        public void render(Map<String, ?> m, HttpServletRequest request, HttpServletResponse response) throws
            Exception
        {
            final Map<String, Object> model = (Map<String, Object>) m;

            final ApplicationModel appModel = runtimeApplication.getApplicationModel();
            final String appName = appModel.getName();
            model.put(TemplateVariables.APP_NAME, appName);
            model.put(TemplateVariables.LOCALE, appModel.matchLocale(request.getLocale()));

            AppAuthentication auth = AppAuthentication.get();
            model.put(TemplateVariables.USER_NAME, auth.getUserName());
            model.put(TemplateVariables.USER_ROLES, auth.getRoles());

            final String contextPath = request.getContextPath();
            model.put(TemplateVariables.CONTEXT_PATH, contextPath);


            final String error = (String) model.get("error");
            if (error != null)
            {
                model.put(TemplateVariables.TITLE, "ERROR");
                model.put(TemplateVariables.CONTENT, "<div class=\"container\"><div class=\"row\"><div class=\"col-md-12\">" +
                    "<h2><span class=\"text-danger glyphicon glyphicon-warning-sign\"></span> Error</h2>\n" +
                    "<p>" + error + "</p>\n" +
                    "<small>STATUS = " + model.get("status") + "<br>Timestamp = " + model.get("timestamp") + "</small></div></div></div>");
            }
            else
            {

                final CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
                model.put(TemplateVariables.CSRF_TOKEN, token.getToken());
                model.put(TemplateVariables.CSRF_HEADER_NAME, token.getHeaderName());
                model.put(TemplateVariables.CSRF_PARAMETER_NAME, token.getParameterName());

                model.put(TemplateVariables.SYSTEM_INFO, systemInfo);
                model.put(TemplateVariables.SCRIPTS,
                    "<script src=\"" + contextPath + "/res/" + appName + "/js/" + servletContext.getAttribute("reactVersion") + "\"></script>\n" +
                        "<script src=\"" + contextPath + "/res/" + appName + "/js/" + servletContext.getAttribute("reactDOMVersion") + "\"></script>\n" +
                        "<script src=\"" + contextPath + "/res/" + appName + "/js/main.js\"></script>\n")
                ;
            }

            //response.setContentLength(data.length);
            response.setContentType(ContentType.HTML);
            response.setCharacterEncoding("UTF-8");

            ServletOutputStream os = null;
            try
            {
                os = response.getOutputStream();
                getTemplate().write(os, model);
                os.flush();
            }
            catch (Exception e)
            {
                IOUtils.closeQuietly(os);
                throw new ExceedRuntimeException("Error sending view", e);
            }
        }


        public BaseTemplate getTemplate()
        {
            if (template == null)
            {
                synchronized (this)
                {
                    if (template == null)
                    {
                        final AppResource resource = runtimeApplication.getResourceLoader()
                            .getResourceLocation(TEMPLATE_RESOURCE).getHighestPriorityResource();
                        final String content = new String(resource.read(), RequestUtil.UTF_8);
                        template = new BaseTemplate(content);
                    }
                }
            }

            return template;
        }


        public void flush()
        {
            template = null;
        }
    }

    private class TemplateChangeListener
        implements ResourceChangeListener
    {
        private final String appName;


        public TemplateChangeListener(String appName)
        {
            this.appName = appName;
        }


        @Override
        public void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String resourcePath)
        {
            if (resourcePath.equals(TEMPLATE_RESOURCE))
            {
                log.debug("Flushing template for application {}", appName);

                applicationViews.get(appName).flush();
            }
        }
    }
}
