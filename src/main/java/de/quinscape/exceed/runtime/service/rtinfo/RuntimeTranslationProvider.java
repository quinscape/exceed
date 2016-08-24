package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ModuleFunctionReferences;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.view.DataProviderContext;
import de.quinscape.exceed.runtime.view.ViewData;
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
public class RuntimeTranslationProvider
    implements RuntimeInfoProvider
{

    private ConcurrentMap<String,Holder> translationReferences = new ConcurrentHashMap<>();


    @Override
    public String getName()
    {
        return "translations";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
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
            Set<String> refs = holder.getReferences(runtimeContext, DataProviderContext.getTranslations(viewData));

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

        public Set<String> getReferences(RuntimeContext runtimeContext, Set<String> providerTranslations)
        {
            if (references == null)
            {
                synchronized (this)
                {
                    if (references == null)
                    {
                        references = findReferences(runtimeContext);
                        references.addAll(providerTranslations);
                    }
                }
            }

            return references;
        }


        private Set<String> findReferences(RuntimeContext runtimeContext)
        {
            StaticFunctionReferences staticFunctionReferences = runtimeContext.getApplicationModel().getMetaData().getStaticFunctionReferences();
            if (staticFunctionReferences == null)
            {
                throw new IllegalStateException("No static function references provided");
            }

            View view = runtimeContext.getView();

            Set<String> referencedTags = new HashSet<>();
            TranslationReferenceVisitor visitor  = new TranslationReferenceVisitor();
            collectTranslationsFromComponent(referencedTags, new HashSet<>(), view.getRoot(), staticFunctionReferences, visitor);

            referencedTags.addAll(visitor.getReferences());

            return referencedTags;
        }


        private void collectTranslationsFromComponent(Set<String> referencedTags, HashSet<String> visitedModules, ComponentModel componentModel, StaticFunctionReferences
            staticFunctionReferences, TranslationReferenceVisitor visitor)
        {
            final ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
            if (componentRegistration != null)
            {
                final ComponentDescriptor descriptor = componentRegistration.getDescriptor();
                String moduleName = componentRegistration.getModuleName();
                if (moduleName == null)
                {
                    throw new IllegalStateException("No module name set in " + componentRegistration);
                }

                collectFromModulesRecursive(referencedTags, visitedModules, moduleName, staticFunctionReferences);

                Attributes attrs = componentModel.getAttrs();

                for (Map.Entry<String, PropDeclaration> entry : descriptor.getPropTypes().entrySet())
                {
                    String propName = entry.getKey();
                    PropDeclaration propDeclaration = entry.getValue();

                    final AttributeValue value = attrs != null ? attrs.getAttribute(propName) : null;
                    if (value != null)
                    {
                        if (value.getAstExpression() != null)
                        {
                            value.getAstExpression().jjtAccept(visitor, null);
                        }
                    }
                    else
                    {
                        // collect default value expression references
                        final AttributeValue defaultValue = propDeclaration.getDefaultValue();
                        if (defaultValue != null && defaultValue.getAstExpression() != null)
                        {
                            defaultValue.getAstExpression().jjtAccept(visitor, null);
                        }
                    }
                }
            }
            else
            {
                Attributes attrs = componentModel.getAttrs();
                if (attrs != null)
                {
                    for (String name : attrs.getNames())
                    {
                        final AttributeValue value = attrs.getAttribute(name);
                        if (value.getAstExpression() != null)
                        {
                            value.getAstExpression().jjtAccept(visitor, null);
                        }
                    }
                }
            }

            for (ComponentModel kid : componentModel.children())
            {
                collectTranslationsFromComponent(referencedTags, visitedModules, kid, staticFunctionReferences, visitor);

            }
        }


        private void collectFromModulesRecursive(Set<String> referencedTags, HashSet<String> visited, String moduleName, StaticFunctionReferences
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
                        referencedTags.addAll(calls);
                    }

                    for (String required : refs.getRequires())
                    {
                        collectFromModulesRecursive(referencedTags, visited, required, staticFunctionReferences);
                    }
                }
            }
        }
    }
}
