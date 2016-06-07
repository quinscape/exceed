package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.context.ScopedElementModel;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ModuleFunctionReferences;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractScopeRelatedProvider
    implements RuntimeInfoProvider
{
    private final static ConcurrentMap<String,Holder> scopeReferences = new ConcurrentHashMap<>();

    protected Set<ScopeReference> getReferences(RuntimeContext runtimeContext, View view)
    {
        String viewName = view.getName();
        Holder holder = new Holder();
        Holder existing = scopeReferences.putIfAbsent(viewName, holder);
        if (existing != null)
        {
            holder = existing;
        }
        return holder.getReferences(runtimeContext);
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
            StaticFunctionReferences staticFunctionReferences = runtimeContext.getRuntimeApplication().getApplicationModel().getStaticFunctionReferences();
            if (staticFunctionReferences == null)
            {
                throw new IllegalStateException("No static function references provided");
            }

            View view = runtimeContext.getView();

            Set<ScopeReference> set = new HashSet<>();
            collect(runtimeContext, set, new HashSet<>(), view.getRoot(), staticFunctionReferences);
            return set;
        }


        private void collect(RuntimeContext runtimeContext, Set<ScopeReference> set, HashSet<String> visitedModules, ComponentModel componentModel, StaticFunctionReferences
            staticFunctionReferences)
        {
            String moduleName = componentModel.getComponentRegistration().getModuleName();
            if (moduleName == null)
            {
                throw new IllegalStateException("No module name set in " + componentModel.getComponentRegistration());
            }

            collectFromModulesRecursive(runtimeContext, set, visitedModules, moduleName, staticFunctionReferences);

            Attributes attrs = componentModel.getAttrs();
            if (attrs != null)
            {
                for (String name : attrs.getNames())
                {
                    ASTExpression astExpression = attrs.getAttribute(name).getAstExpression();
                    if (astExpression != null)
                    {
                        collectExpressionReferences(runtimeContext, set, astExpression);
                    }
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                collect(runtimeContext, set, visitedModules, kid, staticFunctionReferences);
            }
        }


        private void collectExpressionReferences(RuntimeContext runtimeContext, Set<ScopeReference> set, ASTExpression astExpression)
        {
            ScopedValueReferenceVisitor visitor = new ScopedValueReferenceVisitor(runtimeContext);
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
                            set.add(new ScopeReference(type, name, ScopedElementModel.find(runtimeContext, name, type)));
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
