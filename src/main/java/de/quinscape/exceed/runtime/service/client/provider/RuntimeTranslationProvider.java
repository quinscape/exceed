package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.model.meta.ModuleFunctionReferences;
import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.scope.TranslationReferenceVisitor;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@ExceedAppProvider
@ExceedEditorProvider
public class RuntimeTranslationProvider
    implements ClientStateProvider
{

    @Override
    public String getName()
    {
        return "translations";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.REQUEST;
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        Set<String> references = new HashSet<>();
        references.addAll(findReferences(runtimeContext));
        references.addAll(viewData.getTranslations());

        Map<String, Object> map = new HashMap<>();
        for (String code : references)
        {
            map.put(code, runtimeContext.getTranslator().translate(runtimeContext, code));
        }
        return new DefaultClientData(map);
    }


    private Set<String> findReferences(RuntimeContext runtimeContext)
    {
        StaticFunctionReferences staticFunctionReferences = runtimeContext.getApplicationModel().getMetaData().getStaticFunctionReferences();
        if (staticFunctionReferences == null)
        {
            throw new IllegalStateException("No static function references provided");
        }

        View view = runtimeContext.getView();

        if (view == null)
        {
            return Collections.emptySet();
        }

        Set<String> referencedTags = new HashSet<>();
        TranslationReferenceVisitor visitor  = new TranslationReferenceVisitor();
        for (ComponentModel componentModel : view.getContent().values())
        {
            collectTranslationsFromComponent(referencedTags, new HashSet<>(), componentModel,
                staticFunctionReferences, visitor);
        }

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

            staticFunctionReferences.collectReferencesFromModule(moduleName, Translator.I18N_CALL_NAME, referencedTags, visitedModules);

            Attributes attrs = componentModel.getAttrs();

            for (Map.Entry<String, PropDeclaration> entry : descriptor.getPropTypes().entrySet())
            {
                String propName = entry.getKey();
                PropDeclaration propDeclaration = entry.getValue();

                final ExpressionValue value = attrs != null ? attrs.getAttribute(propName) : null;
                visitor.visit(value);
                // collect default value expression references
                visitor.visit(propDeclaration.getDefaultValue());
            }
        }
        else
        {
            Attributes attrs = componentModel.getAttrs();
            if (attrs != null)
            {
                for (String name : attrs.getNames())
                {
                    final ExpressionValue value = attrs.getAttribute(name);
                    visitor.visit(value);
                }
            }
        }

        for (ComponentModel kid : componentModel.children())
        {
            collectTranslationsFromComponent(referencedTags, visitedModules, kid, staticFunctionReferences, visitor);

        }
    }


}
