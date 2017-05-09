package de.quinscape.exceed.runtime.service.client;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.util.RequestUtil;
import de.quinscape.exceed.runtime.view.ViewData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides client state data structures for the client side view based on a list of {@link ClientStateProvider}
 * implementations and handles the caching / scoping of those values.
 *
 */
@Service
public class ClientStateService
{
    private final static Logger log = LoggerFactory.getLogger(ClientStateService.class);

    private static final String USER_KEY_PREFIX = "user:";

    private final ConcurrentMap<ScopeKey, Holder> cachedValues = new ConcurrentHashMap<>();

    /**
     * Returns the client state JSON block for the current request
     *
     * @param request           servlet request
     * @param runtimeContext    runtime context
     * @param viewData          collected view data
     * @param providers         set of client state providers to use
     * @return
     */
    public String getClientStateJSON(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData, Set<ClientStateProvider> providers)
    {
        try
        {


            final JSONBuilder builder = JSONBuilder.buildObject();

            // insert providers of mutable data directly into the JSON root
            invokeProviders(builder, request, runtimeContext, viewData, providers, true);

            // move providers of immutable data into the "meta" block.
            builder.objectProperty("meta");
            invokeProviders(builder, request, runtimeContext, viewData, providers, false);
            builder.close();

            return builder.output();
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private void invokeProviders(JSONBuilder builder, HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData, Set<ClientStateProvider> providers, boolean mutable) throws Exception

    {
        final boolean isAjaxRequest = RequestUtil.isAjaxRequest(request);

        for (ClientStateProvider provider : providers)
        {
            if (provider.isMutable() == mutable && !(isAjaxRequest && provider.isSkippedOnAjax()))
            {
                invokeProvider(request, runtimeContext, viewData, builder, provider);
            }
        }
    }


    public void invokeProvider(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData, JSONBuilder builder, ClientStateProvider provider) throws Exception
    {
        final String clientStateName = provider.getName();
        final ClientStateScope scope = provider.getScope();

        final ClientData clientStateResult;
        if (scope == ClientStateScope.REQUEST)
        {
            log.debug("Provide '{}'", clientStateName);
            clientStateResult = provider.provide(request, runtimeContext, viewData);
        }
        else
        {
            if (runtimeContext.getView() == null && scope == ClientStateScope.VIEW)
            {
                return;
            }

            final ScopeKey key = new ScopeKey(scope, getIdentifier(runtimeContext, scope), provider);

            Holder holder = new Holder(provider);
            final Holder existing = cachedValues.putIfAbsent(key, holder);
            if (existing != null)
            {
                holder = existing;

                if (log.isDebugEnabled())
                {
                    log.debug("Reuse '{}' ({})", clientStateName,  key);
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Provide '{}' ({})", clientStateName,  key);
                }
            }

            clientStateResult = holder.provide(request, runtimeContext, viewData);
        }

        if (clientStateResult != null)
        {
            builder.includeProperty(clientStateName, clientStateResult.getJSON());
        }
    }
    private final static String APP_KEY_PREFIX = "app:";
    
    private final static String VERSION_KEY_PREFIX = "version:";


    private String getIdentifier(RuntimeContext runtimeContext, ClientStateScope scope)
    {
        switch(scope)
        {
            case USER:
                // TODO: flush on logout
                return getUserIdentifier(runtimeContext.getAuthentication().getUserName());
            case APPLICATION:
                return getAppIdentifier(runtimeContext.getApplicationModel().getName());
            case MODEL_VERSION:
                return getVersionIdentifier(runtimeContext.getApplicationModel().getName());
            case VIEW:
                final View view = runtimeContext.getView();
                if (view == null)
                {
                    throw new IllegalStateException("No view for view scoped client state");
                }
                return view.getName() + ":" + view.getVersionGUID();
            case REQUEST:
                throw new IllegalStateException("Request scope not cached");
            default:
                throw new IllegalStateException("Unhandled scope type " + scope);
        }
    }


    private String getUserIdentifier(String userName)
    {
        return USER_KEY_PREFIX + userName;
    }


    /**
     * Removes the cached client state results associated with the given view.
     *
     * @param view  view model
     */
    public void flushViewScope(View view)
    {
        final String viewName = view.getName();
        cachedValues.keySet().removeIf(
            scopeKey ->
                scopeKey.getScope() == ClientStateScope.VIEW && scopeKey.getIdentifier().equals(viewName)
        );
    }


    /**
     * Removes all application scoped client state results.
     */
    public void flushApplicationScope(String appName)
    {
        cachedValues.keySet().removeIf(
            scopeKey ->
                scopeKey.getScope() == ClientStateScope.APPLICATION && scopeKey.getIdentifier().equals(getAppIdentifier(appName))
        );
    }


    private String getAppIdentifier(String appName)
    {
        return APP_KEY_PREFIX + appName;
    }


    public void flushModelVersionScope( String appName)
    {
        String identifier = getVersionIdentifier(appName);

        cachedValues.keySet().removeIf(
            scopeKey -> {
                final ClientStateScope scope = scopeKey.getScope();
                if (scope != ClientStateScope.MODEL_VERSION)
                {
                    return false;
                }
                return  scopeKey.getIdentifier().equals(identifier);
            }
        );
    }


    public String getVersionIdentifier(String appName)
    {
        return VERSION_KEY_PREFIX + appName;
    }


    private class ScopeKey
    {

        private final ClientStateScope scope;
        
        private final String identifier;

        /**
         * We add the provider to the key and use instance equality for it to make sure we have one
         * cache entry per client state provider instance to make sure we're not hit by a provider
         * generating different content in different usage scenarios
         */
        private final ClientStateProvider provider;


        public ScopeKey(ClientStateScope scope, String identifier, ClientStateProvider provider)
        {
            this.scope = scope;
            this.identifier = identifier;
            this.provider = provider;
        }


        public ClientStateScope getScope()
        {
            return scope;
        }


        public String getIdentifier()
        {
            return identifier;
        }

        @Override
        public int hashCode()
        {
            return (scope.hashCode() + 37 * identifier.hashCode()) * 37 + provider.hashCode();
        }


        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }

            if (obj instanceof ScopeKey)
            {
                ScopeKey that = (ScopeKey) obj;
                return
                    this.scope == that.scope &&
                    this.identifier.equals(that.identifier) &&
                    // instance equality check for the provider
                    this.provider == that.provider;
            }
            return false;
        }


        @Override
        public String toString()
        {
            return scope + "-" + identifier;
        }
    }


    private class Holder
    {

        private final ClientStateProvider provider;
        private volatile ClientData data;


        public Holder(ClientStateProvider provider)
        {

            this.provider = provider;
        }


        public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
            Exception
        {
            if (data == null)
            {
                synchronized (this)
                {
                    if (data == null)
                    {
                        data = provider.provide(request, runtimeContext, viewData);
                    }
                }
            }

            return data;
        }
    }


    /**
     * Returns a set of ClientStateProvider beans annotated with the given annotation
     *
     * @param applicationContext    spring application context
     * @param anno                  annotation class which must be annotated with @ProviderQualifier
     * @return
     */
    public static Set<ClientStateProvider> findProviderBeans(ApplicationContext applicationContext, Class<? extends Annotation> anno)
    {
        if (anno.getAnnotation(ProviderQualifier.class) == null)
        {
            throw new ExceedRuntimeException("Annotation type @" + anno.getSimpleName() + " is not annotated with @ProviderQualifier");
        }

        final Collection<?> values = applicationContext.getBeansWithAnnotation(anno).values();

        final HashSet<ClientStateProvider> providers = new HashSet<>();
        for (Object value : values)
        {
            if ((value instanceof ClientStateProvider))
            {
                providers.add((ClientStateProvider) value);
            }
            else
            {
                throw new ExceedRuntimeException(value + " is annotated with @" + anno.getSimpleName() + ", but does not implement  " + ClientStateProvider.class.getName());
            }
        }
        return providers;
    }
}
