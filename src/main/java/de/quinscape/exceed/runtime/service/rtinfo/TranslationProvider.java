package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ModuleFunctionReferences;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class TranslationProvider
    implements RuntimeInfoProvider
{

    private ConcurrentMap<String,Holder> translationReferences = new ConcurrentHashMap<>();


    @Override
    public String getName()
    {
        return "translations";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        View view = runtimeContext.getView();
        if (view != null)
        {
            String viewName = view.getName();
            Holder holder = new Holder();
            Holder existing = translationReferences.putIfAbsent(viewName, holder);
            if (existing != null)
            {
                holder = existing;
            }
            Set<String> refs = holder.getReferences(runtimeContext);

            Map<String, Object> map = new HashMap<>();
            for (String code : refs)
            {
                map.put(code, runtimeContext.getTranslator().translate(runtimeContext, code));
            }
            return map;
        }
        return null;
    }


    private static class Holder
    {
        private volatile Set<String> references;

        public Set<String> getReferences(RuntimeContext runtimeContext)
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


        private Set<String> findReferences(RuntimeContext runtimeContext)
        {
            StaticFunctionReferences staticFunctionReferences = runtimeContext.getApplicationModel().getStaticFunctionReferences();
            if (staticFunctionReferences == null)
            {
                throw new IllegalStateException("No static function references provided");
            }

            View view = runtimeContext.getView();

            Set<String> set = new HashSet<>();
            collectTranslationsFromComponent(set, new HashSet<>(), view.getRoot(), staticFunctionReferences);
            return set;
        }


        private void collectTranslationsFromComponent(Set<String> set, HashSet<String> visitedModules, ComponentModel componentModel, StaticFunctionReferences
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
                        collectTranslationsFromExpressions(set, astExpression);
                    }
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                collectTranslationsFromComponent(set, visitedModules, kid, staticFunctionReferences);
            }
        }


        private void collectTranslationsFromExpressions(Set<String> set, ASTExpression astExpression)
        {
            TranslationReferenceVisitor visitor = new TranslationReferenceVisitor();
            astExpression.jjtAccept(visitor, null);
            set.addAll(visitor.getReferences());
        }


        private void collectFromModulesRecursive(Set<String> set, HashSet<String> visited, String moduleName, StaticFunctionReferences
            staticFunctionReferences)
        {
            if (!visited.contains(moduleName))
            {
                visited.add(moduleName);

                ModuleFunctionReferences refs = staticFunctionReferences.getModuleFunctionReferences(moduleName);
                if (refs != null)
                {
                    List<String> calls = refs.getCalls(Translator.I18N_CALL_NAME);
                    if (calls != null && calls.size() != 0)
                    {
                        set.addAll(calls);
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
