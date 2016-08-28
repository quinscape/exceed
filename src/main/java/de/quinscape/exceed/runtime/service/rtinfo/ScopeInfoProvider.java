package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.model.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ViewContext;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@Service
public class ScopeInfoProvider
    implements RuntimeInfoProvider
{

    @Autowired
    private ScopeReferenceService scopeReferenceService;

    @Override
    public String getName()
    {
        return "scopeInfo";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        View view = runtimeContext.getView();
        if (view != null)
        {
            Map<String, Boolean> viewContext = new HashMap<>();
            for (ScopeReference reference : scopeReferenceService.getClientReferences(runtimeContext, view))
            {
                if (ViewContext.class.isAssignableFrom(reference.getScopeType()))
                {
                    viewContext.put(reference.getName(), true);
                }
            }

            final Set<String> queryViewContextRefs = new HashSet<>();
            for (ScopeReference reference : scopeReferenceService.getServerReferences(runtimeContext, view))
            {
                queryViewContextRefs.add(reference.getName());
            }

            final Map<String, TransitionViewScopeReference> transitionViewScopeReferences =
                scopeReferenceService.getTransitionViewScopeReferences(runtimeContext, view);

            return new ScopeInfo(viewContext, queryViewContextRefs, transitionViewScopeReferences);
        }
        return null;
    }


    public static class ScopeInfo
    {
        private final Map<String, Boolean> viewContext;

        private final Set<String> queryRefs;

        private final Map<String, TransitionViewScopeReference> transitionRefs;


        public ScopeInfo(Map<String,
            Boolean> viewContext, Set<String> queryRefs, Map<String, TransitionViewScopeReference>
                             transitionRefs)
        {
            this.viewContext = viewContext;
            this.queryRefs = queryRefs;
            this.transitionRefs = transitionRefs;
        }


        public Map<String, Boolean> getViewContext()
        {
            return viewContext;
        }


        public Map<String, TransitionViewScopeReference> getTransitionRefs()
        {
            return transitionRefs;
        }


        public Set<String> getQueryRefs()
        {
            return queryRefs;
        }
    }

}
