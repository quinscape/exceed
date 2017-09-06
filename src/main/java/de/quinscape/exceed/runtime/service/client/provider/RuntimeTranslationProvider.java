package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.context.ScopeDeclaration;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.scope.TranslationReferenceVisitor;
import de.quinscape.exceed.runtime.view.ViewData;

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
        return ClientStateScope.VIEW;
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }


    @Override
    public boolean isSkippedOnAjax()
    {
        return true;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        TranslationReferenceVisitor visitor  = new TranslationReferenceVisitor();

        findScopeReference(runtimeContext, visitor, viewData);
        findReferences(runtimeContext, visitor);

        final Set<String> references = visitor.getReferences();

        references.addAll(viewData.getTranslations());

        Map<String, Object> map = new HashMap<>();
        for (String code : references)
        {
            map.put(code, runtimeContext.getTranslator().translate(runtimeContext, code));
        }
        return new DefaultClientData(map);
    }


    private void findScopeReference(
        RuntimeContext runtimeContext, TranslationReferenceVisitor visitor,
        ViewData viewData
    )
    {
        final String scopeLocation = runtimeContext.getScopedContextChain().getScopeLocation();

        final ScopeDeclarations declarations = runtimeContext.getApplicationModel().lookup(scopeLocation);

        for (ScopeDeclaration declaration : declarations.getDeclarations().values())
        {
            final ScopedPropertyModel propertyModel = declaration.getModel();
            viewData.registerTranslation(runtimeContext, propertyModel);

            final ASTExpression defaultValueExpression = propertyModel.getDefaultValueExpression();
            if (defaultValueExpression != null)
            {
                visitor.visit(defaultValueExpression, null);
            }
        }
    }


    private void findReferences(RuntimeContext runtimeContext, TranslationReferenceVisitor visitor)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();
        View view = runtimeContext.getView();

        if (view != null)
        {
            findViewTranslationReferences(visitor, applicationModel, view);
        }

        for (DomainRule domainRule : applicationModel.getDomainRules().values())
        {
            final ExpressionValue errorMessageValue = domainRule.getErrorMessageValue();
            visitor.visit(domainRule.getRuleValue());
            visitor.visit(domainRule.getErrorMessageValue());
        }
    }


    private void findViewTranslationReferences(TranslationReferenceVisitor visitor, ApplicationModel applicationModel, View view)
    {
        StaticFunctionReferences staticFunctionReferences = applicationModel.getMetaData().getStaticFunctionReferences();
        if (staticFunctionReferences == null)
        {
            throw new IllegalStateException("No static function references provided");
        }

        final Set<String> visitedModules = new HashSet<>();

        for (ComponentModel componentModel : view.getContent().values())
        {
            collectTranslationsFromComponent(
                componentModel,
                visitor,
                staticFunctionReferences,
                visitedModules
            );
        }
    }


    private void collectTranslationsFromComponent(ComponentModel componentModel, TranslationReferenceVisitor visitor, StaticFunctionReferences
        staticFunctionReferences, Set<String> visitedModules)
    {
        final ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();
        if (componentRegistration != null)
        {
            final ComponentDescriptor descriptor = componentRegistration.getDescriptor();
            String moduleName = componentRegistration.getModuleName();
            if (moduleName == null)
            {
                throw new IllegalStateException("No module name set in " + componentRegistration);
            }

            staticFunctionReferences.collectReferencesFromModule(moduleName, Translator.I18N_CALL_NAME, visitor.getReferences(), visitedModules);

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
            collectTranslationsFromComponent(kid, visitor, staticFunctionReferences, visitedModules);
        }
    }
}
