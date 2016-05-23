package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ModuleFunctionReferences;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@Service
public class ScopedValuesProvider
    implements RuntimeInfoProvider
{
    private ConcurrentMap<String,Holder> scopeReferences = new ConcurrentHashMap<>();


    @Override
    public String getName()
    {
        return "scope";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        View view = runtimeContext.getView();
        if (view != null)
        {
            String viewName = view.getName();
            Holder holder = new Holder();
            Holder existing = scopeReferences.putIfAbsent(viewName, holder);
            if (existing != null)
            {
                holder = existing;
            }
            Set<ScopeReference> refs = holder.getReferences(runtimeContext);
            ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

            Map<String, Object> map = new HashMap<>();
            for (ScopeReference reference : refs)
            {
                String name = reference.getName();
                map.put(name, reference.getType().get(scopedContextChain, name));
            }
            return map;
        }
        return null;
    }


    private static class Holder
    {
        private volatile Set<ScopeReference> references;

        public Set<ScopeReference> getReferences(RuntimeContext runtimeContext)
        {
            if (references == null)
            {
                synchronized (this)
                {
                    if (references == null)
                    {
                        references = findReferences(runtimeContext);

                    }
                }
            }

            return references;
        }


        private Set<ScopeReference> findReferences(RuntimeContext runtimeContext)
        {
            StaticFunctionReferences staticFunctionReferences = runtimeContext.getRuntimeApplication().getStaticFunctionReferences();
            if (staticFunctionReferences == null)
            {
                throw new IllegalStateException("No static function references provided");
            }

            View view = runtimeContext.getView();

            Set<ScopeReference> set = new HashSet<>();
            collect(set, new HashSet<>(), view.getRoot(), staticFunctionReferences);
            return set;
        }


        private void collect(Set<ScopeReference> set, HashSet<String> visitedModules, ComponentModel componentModel, StaticFunctionReferences
            staticFunctionReferences)
        {
            String moduleName = componentModel.getComponentRegistration().getModuleName();
            if (moduleName == null)
            {
                throw new IllegalStateException("No module name set in " + componentModel.getComponentRegistration());
            }

            collectFromModulesRecursive(set, visitedModules, moduleName, staticFunctionReferences);

            Attributes attrs = componentModel.getAttrs();
            if (attrs != null)
            {
                for (String name : attrs.getNames())
                {
                    ASTExpression astExpression = attrs.getAttribute(name).getAstExpression();
                    if (astExpression != null)
                    {
                        collectExpressionReferences(set, astExpression);
                    }
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                collect(set, visitedModules, kid, staticFunctionReferences);
            }
        }


        private void collectExpressionReferences(Set<ScopeReference> set, ASTExpression astExpression)
        {
            ScopedValueReferenceVisitor visitor = new ScopedValueReferenceVisitor();
            astExpression.jjtAccept(visitor, null);
            set.addAll(visitor.getReferences());
        }


        private void collectFromModulesRecursive(Set<ScopeReference> set, HashSet<String> visited, String moduleName, StaticFunctionReferences
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
                        for (String name : refs.getCalls(type.name()))
                        {
                            set.add(new ScopeReference(type, name));
                        }
                    }

                    for (String required : refs.getRequires())
                    {
                        collectFromModulesRecursive(set, visited, required, staticFunctionReferences);
                    }
                }
            }
        }
    }
}
