package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.model.component.ModuleFunctionReferences;
import de.quinscape.exceed.model.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
import de.quinscape.exceed.runtime.scope.ViewContext;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides sets of scope references for views / components:
 *
 * <ul>
 *     <li>Scope references within component expressions and js code</li>
 *     <li>View-Scope references in component queries</li>
 * </ul>
 *
 */
@Service
public class ScopeReferenceService
{
    private final ConcurrentMap<String,Holder> references = new ConcurrentHashMap<>();

    public Set<ScopeReference> getClientReferences(RuntimeContext runtimeContext, View view)
    {
        return holderForView(view).getClientReferences(runtimeContext);
    }

    public Set<ScopeReference> getServerReferences(RuntimeContext runtimeContext, View view)
    {
        return holderForView(view).getServerReferences(runtimeContext);
    }

    public Map<String, TransitionViewScopeReference> getTransitionViewScopeReferences(RuntimeContext runtimeContext, View view)
    {
        return holderForView(view).getTransitionViewScopeReferences(runtimeContext);
    }


    private Holder holderForView(View view)
    {
        String viewName = view.getName();
        Holder holder = new Holder(view.getVersion());
        Holder existing = references.putIfAbsent(viewName, holder);
        if (existing != null)
        {
            if (existing.getViewVersion().equals(view.getVersion()))
            {
                holder = existing;
            }
            else
            {
                // view changed, use new holder to reevaluate references
                // non-synchronized because a) code reloading should only happen in dev anyway and b) then we can
                // live with other threads seeing the old holder and the reinit happening multiple times.
                references.put(viewName, holder);
            }
        }
        return holder;
    }


    private static class Holder
    {
        private final String viewVersion;

        /**
         * References to scopes in client components.
         */
        private volatile Set<ScopeReference> clientReferences;

        /**
         * References to view scope in server query expressions.
         */
        private volatile Set<ScopeReference> serverReferences;

        private volatile Map<String,TransitionViewScopeReference> transitionViewScopeReferences;


        public Holder(String viewVersion)
        {
            this.viewVersion = viewVersion;
        }


        public String getViewVersion()
        {
            return viewVersion;
        }


        public Set<ScopeReference> getClientReferences(RuntimeContext runtimeContext)
        {
            if (clientReferences == null)
            {
                synchronized (this)
                {
                    if (clientReferences == null)
                    {
                        clientReferences = findClientReferences(runtimeContext);
                        serverReferences = findServerReferences(runtimeContext);

                        View view = runtimeContext.getView();
                        if (view.isContainedInProcess())
                        {
                            transitionViewScopeReferences = findTransitionViewScopeReferences(runtimeContext);
                        }
                        else
                        {
                            transitionViewScopeReferences = Collections.emptyMap();
                        }
                    }
                }
            }

            return clientReferences;
        }


        private Map<String, TransitionViewScopeReference> findTransitionViewScopeReferences(RuntimeContext runtimeContext)

        {
            Map<String, TransitionViewScopeReference> map = new HashMap<>();

            final View view = runtimeContext.getView();
            final ViewState viewState = view.getViewState(runtimeContext);

            final Map<String, Transition> transitions = viewState.getTransitions();
            if (transitions != null)
            {
                for (Map.Entry<String, Transition> entry : transitions.entrySet())
                {
                    final String name = entry.getKey();
                    final Transition transition = entry.getValue();

                    boolean noStateChange = transition.getTo().equals(view.getLocalName());

                    final ASTExpression actionAST = transition.getActionAST();
                    if (actionAST != null)
                    {
                        final TransitionScopeReferenceCollector collector = new TransitionScopeReferenceCollector(runtimeContext);
                        actionAST.jjtAccept(collector, null);

                        map.put(name, new TransitionViewScopeReference(collector.getReferences(), noStateChange && collector.isViewContextOnly()));
                    }
                    else
                    {
                        map.put(name, new TransitionViewScopeReference(Collections.emptySet(), noStateChange));
                    }
                }
            }

            return Collections.unmodifiableMap(map);
        }


        public Set<ScopeReference> getServerReferences(RuntimeContext runtimeContext)
        {
            getClientReferences(runtimeContext);
            return serverReferences;
        }


        public Map<String, TransitionViewScopeReference> getTransitionViewScopeReferences(RuntimeContext runtimeContext)
        {
            getClientReferences(runtimeContext);
            return transitionViewScopeReferences;
        }


        private Set<ScopeReference> findServerReferences(RuntimeContext runtimeContext)
        {
            View view = runtimeContext.getView();
            Set<ScopeReference> set = new HashSet<>();
            collectServerReferences(runtimeContext, set, view.getRoot());
            return Collections.unmodifiableSet(set);
        }


        private void collectServerReferences(RuntimeContext runtimeContext, Set<ScopeReference> set, ComponentModel componentModel)
        {

            if (componentModel.isComponent())
            {
                final ComponentDescriptor descriptor = componentModel.getComponentRegistration().getDescriptor();

                for (Object query : descriptor.getQueries().values())
                {
                    if (query instanceof String)
                    {
                        try
                        {
                            final ASTExpression expression = ExpressionParser.parse((String) query);

                            final ScopeReferenceCollector collector = new ScopeReferenceCollector(runtimeContext, ViewContext.class, componentModel);

                            // direct query expression references (should very rarely if ever happen)
                            expression.jjtAccept(collector, null);

                            final PropReferenceCollector propReferenceCollector = new PropReferenceCollector();

                            expression.jjtAccept(propReferenceCollector, null);

                            for (String propName : propReferenceCollector.getReferences())
                            {
                                final PropDeclaration propDeclaration = descriptor.getPropTypes().get(propName);

                                AttributeValue attr = componentModel.getAttribute(propName);
                                if (attr == null)
                                {
                                    attr = propDeclaration.getDefaultValue();
                                }

                                if (attr != null && attr.getAstExpression() != null)
                                {
                                    // collect references from either component expressions or default value expressions
                                    attr.getAstExpression().jjtAccept(collector, null);
                                }
                            }

                            set.addAll(collector.getReferences());
                        }
                        catch (ParseException e)
                        {
                            throw new ExceedRuntimeException(e);
                        }
                    }
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                collectServerReferences(runtimeContext, set, kid);
            }
        }


        private Set<ScopeReference> findClientReferences(RuntimeContext runtimeContext)
        {
            StaticFunctionReferences staticFunctionReferences = runtimeContext.getApplicationModel().getMetaData().getStaticFunctionReferences();
            if (staticFunctionReferences == null)
            {
                throw new IllegalStateException("No static function references provided");
            }

            View view = runtimeContext.getView();

            Set<ScopeReference> set = new HashSet<>();
            collectClientReferences(runtimeContext, set, new HashSet<>(), view.getRoot(), staticFunctionReferences);
            return Collections.unmodifiableSet(set);
        }


        private void collectClientReferences(RuntimeContext runtimeContext, Set<ScopeReference> set, HashSet<String> visitedModules, ComponentModel componentModel, StaticFunctionReferences
            staticFunctionReferences)
        {
            final ComponentRegistration componentRegistration = componentModel.getComponentRegistration();


            if (componentRegistration != null)
            {
                String moduleName = componentRegistration.getModuleName();
                if (moduleName == null)
                {
                    throw new IllegalStateException("No module name set in " + componentRegistration);
                }

                collectFromModulesRecursive(runtimeContext, set, visitedModules, moduleName, staticFunctionReferences);
            }

            Attributes attrs = componentModel.getAttrs();
            if (attrs != null)
            {
                for (String name : attrs.getNames())
                {
                    ASTExpression astExpression = attrs.getAttribute(name).getAstExpression();
                    if (astExpression != null)
                    {
                        collectExpressionReferences(runtimeContext, set, astExpression, componentModel);
                    }
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                collectClientReferences(runtimeContext, set, visitedModules, kid, staticFunctionReferences);
            }
        }


        private void collectExpressionReferences(RuntimeContext runtimeContext, Set<ScopeReference> set, ASTExpression astExpression, ComponentModel componentModel)
        {
            ScopeReferenceCollector visitor = new ScopeReferenceCollector(runtimeContext, componentModel);
            astExpression.jjtAccept(visitor, null);
            set.addAll(visitor.getReferences());
        }


        private void collectFromModulesRecursive(RuntimeContext runtimeContext, Set<ScopeReference> set, HashSet<String> visited, String moduleName, StaticFunctionReferences
            staticFunctionReferences)
        {
            if (!visited.contains(moduleName))
            {
                visited.add(moduleName);

                ModuleFunctionReferences refs = staticFunctionReferences.getModuleFunctionReferences(moduleName);
                if (refs != null)
                {
                    for (ScopedValueType type : ScopedValueType.values())
                    {
                        // we're simply using the name of ScopedValueType as our static call definition names
                        for (String name : refs.getCalls(type.name()))
                        {
                            ScopedContext scope = type.findScope(runtimeContext.getScopedContextChain(), name);
                            set.add(new ScopeReference(type, name, scope.getClass(), scope.getModel(type, name)));
                        }
                    }

                    for (String required : refs.getRequires())
                    {
                        collectFromModulesRecursive(runtimeContext, set, visited, required, staticFunctionReferences);
                    }
                }
            }
        }
    }
}
