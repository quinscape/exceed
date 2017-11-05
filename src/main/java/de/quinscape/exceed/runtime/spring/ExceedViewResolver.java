package de.quinscape.exceed.runtime.spring;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.config.BaseTemplateConfig;
import de.quinscape.exceed.model.meta.WebpackEntryPoint;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.config.WebpackConfig;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.template.BaseTemplate;
import de.quinscape.exceed.runtime.template.TemplateVariables;
import de.quinscape.exceed.runtime.template.TemplateVariablesProvider;
import de.quinscape.exceed.runtime.util.ContentType;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resolves the react.js client side view identifiers to an {@link ExceedView}.
 *
 * The view name must be of the format "appName:bundleA,bundleB" to load the base template of the app "appName" with
 * the webpack script bundles "bundleA" and "bundleB".
 *
 * We use {@link WebpackConfig#APP_BUNDLES} and {@link WebpackConfig#EDITOR_MAIN_MODULE} values inside the default application.
 */
public class ExceedViewResolver
    implements ViewResolver
{

    private final static Logger log = LoggerFactory.getLogger(ExceedViewResolver.class);

    private static final String TEMPLATE_RESOURCE = "/resources/template/template.html";

    private final ApplicationContext applicationContext;

    private final Collection<TemplateVariablesProvider> templateVariablesProviders;

    private volatile ApplicationService applicationService;

    private final ServletContext servletContext;

    private ConcurrentMap<ViewSpec, ExceedView> applicationViews = new ConcurrentHashMap<>();


    public ExceedViewResolver(ApplicationContext applicationContext, ServletContext servletContext, Collection
        <TemplateVariablesProvider> templateVariablesProviders)
    {
        this.applicationContext = applicationContext;
        this.servletContext = servletContext;
        this.templateVariablesProviders = templateVariablesProviders;
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


    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception
    {
        ViewSpec spec = new ViewSpec(viewName);


        final RuntimeApplication runtimeApplication = getApplicationService().getRuntimeApplication(servletContext,
            spec.appName);

        ExceedView holder = new ExceedView(runtimeApplication, servletContext, templateVariablesProviders, spec.bundles);
        final ExceedView existing = applicationViews.putIfAbsent(spec, holder);
        if (existing == null)
        {
            // register for change events
            final TemplateChangeListener listener = new TemplateChangeListener(spec.appName);
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



    private static class ExceedView
        implements View
    {

        private final Collection<TemplateVariablesProvider> templateVariablesProviders;

        private volatile BaseTemplate template;

        private final RuntimeApplication runtimeApplication;

        private final ServletContext servletContext;

        private final List<String> scriptBundles;

        private ExceedView(RuntimeApplication runtimeApplication, ServletContext servletContext,
                           Collection<TemplateVariablesProvider> templateVariablesProviders, List<String> scriptBundles)
        {
            this.runtimeApplication = runtimeApplication;
            this.servletContext = servletContext;
            this.scriptBundles = scriptBundles;

            if (templateVariablesProviders != null)
            {
                this.templateVariablesProviders = templateVariablesProviders;
            }
            else
            {
                this.templateVariablesProviders = Collections.emptyList();
            }
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
            model.put(TemplateVariables.LOCALE, appModel.getConfigModel().matchLocale(request.getLocale()));

            final String contextPath = request.getContextPath();
            model.put(TemplateVariables.APP_NAME, appName);
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

                final List<WebpackEntryPoint> entries = appModel.getMetaData().getWebpackStats().getEntries();
                StringBuilder scriptsBuilder = new StringBuilder();

                for (String bundle : scriptBundles)
                {
                    boolean found = false;
                    for (WebpackEntryPoint entryPoint : entries)
                    {
                        if (entryPoint.getChunkNames().contains(bundle))
                        {
                            scriptsBuilder.append("<script src=\"")
                                .append(contextPath)
                                .append("/res/")
                                .append(appName)
                                .append("/js/")
                                .append(entryPoint.getName())
                                .append("\"></script>\n");

                            found = true;
                            break;
                        }
                    }

                    if (!found)
                    {
                        throw new ExceedRuntimeException("Webpack script bundle '" + bundle + "' not found.");
                    }
                }

                model.put(TemplateVariables.SCRIPTS, scriptsBuilder.toString());

                final BaseTemplateConfig baseTemplateConfig = appModel.getConfigModel().getComponentConfig().getBaseTemplateConfig();
                if (baseTemplateConfig != null)
                {
                    for (String name : baseTemplateConfig.propertyNames())
                    {
                        model.put(name, baseTemplateConfig.getProperty(name));
                    }
                }

                if (templateVariablesProviders.size() > 0)
                {
                    final RuntimeContext runtimeContext = RuntimeContextHolder.get(request);

                    templateVariablesProviders.forEach(provider -> provider.provide(runtimeContext, model));
                }
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
            catch (IOException e)
            {
                IOUtils.closeQuietly(os);

                // these are most commonly browser windows being closed before the last request is done
                log.debug("Error sending view", e);
            }
            catch (Exception e)
            {
                IOUtils.closeQuietly(os);
                // these are most commonly browser windows being closed before the last request is done
                log.error("Error sending view", e);
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
                            .getResources(TEMPLATE_RESOURCE).getHighestPriorityResource();
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


        @Override
        public String toString()
        {
            return super.toString() + ": "
                + "appName = '" + appName + '\''
                ;
        }
    }

    private final class ViewSpec
    {
        public final String appName;
        public final List<String> bundles;

        private ViewSpec(String viewName)
        {
            if (viewName == null)
            {
                throw new IllegalArgumentException("viewName can't be null");
            }

            int pos = viewName.indexOf(":");
            if (pos < 0)
            {
                this.appName = getApplicationService().getDefaultApplication();
                this.bundles = Util.splitAtComma(viewName);
            }
            else
            {
                this.appName = viewName.substring(0, pos);
                this.bundles = Util.splitAtComma(viewName.substring(pos + 1));
            }
        }


        @Override
        public int hashCode()
        {
            return Util.hashcodeOver(appName, bundles);
        }


        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            
            if (obj instanceof ViewSpec)
            {
                ViewSpec that = (ViewSpec) obj;

                return this.appName.equals(that.appName) &&
                    this.bundles.equals(that.bundles);
            }
            return false;
        }
    }
}
