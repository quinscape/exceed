package de.quinscape.exceed.model.translation;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.model.ApplicationConfig;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.meta.ModuleFunctionReferences;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.i18n.TranslationProvider;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.client.scope.TranslationReferenceVisitor;
import org.svenson.JSONTypeHint;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Encapsulates the editor state for the translation editor.
 *
 * A UI-centric view on the current translation database state.
 */
public class TranslationEditorState
    extends Model
{
    private final TranslationProvider translationProvider;

    private Map<String,TranslationEntry> translations = new HashMap<>();

    public TranslationEditorState(RuntimeContext runtimeContext, TranslationProvider translationProvider)
    {
        this.translationProvider = translationProvider;

        loadDBTranslations(runtimeContext);
        addModuleReferences(runtimeContext);
        addDomainNameReferences(runtimeContext);
        addLanguageReferences(runtimeContext);
        addComponentReferences(runtimeContext);
        fillInUnqualifiedReferences();

        final ApplicationConfig configModel = runtimeContext.getApplicationModel().getConfigModel();

        initializeDefaults(runtimeContext, configModel.getSupportedLocales());
    }


    @JSONTypeHint(TranslationEntry.class)
    public Map<String, TranslationEntry> getTranslations()
    {
        return translations;
    }


    private void addTranslations(DomainObject translation)
    {
        final String tag = (String) translation.getProperty("tag");
        final String locale = (String) translation.getProperty("locale");
        final String processName = (String) translation.getProperty("processName");
        final String viewName = (String) translation.getProperty("viewName");

        TranslationEntry entry = entry(tag);

        if (processName == null && viewName == null)
        {
            entry.getTranslations().put(locale, translation);
        }
        else
        {
            entry.getLocalTranslations().add(translation);
        }
    }


    private TranslationEntry entry(String tag)
    {
        return translations.computeIfAbsent(tag, TranslationEntry::new);
    }


    private void addReference(String tag, ReferenceType type, String name)
    {
        TranslationEntry entry = entry(tag);

        entry.addReference(new ReferenceInfo(type, name));
    }


    private void initializeDefaults(RuntimeContext runtimeContext, List<String> supportedLocales)
    {
        final DomainService domainService = runtimeContext.getDomainService();

        final Timestamp now = new Timestamp(System.currentTimeMillis());

        for (TranslationEntry translationEntry : translations.values())
        {
            for (String locale : supportedLocales)
            {
                final Map<String, DomainObject> translations = translationEntry.getTranslations();
                if (translations.get(locale) == null)
                {
                    final String id = UUID.randomUUID().toString();
                    final DomainObject domainObject = domainService.create("AppTranslation", id);

                    domainObject.setProperty("tag", translationEntry.getName());

                    domainObject.setProperty("created", now);
                    domainObject.setProperty("locale", locale);
                    domainObject.setProperty("translation", "");
                    domainObject.setProperty("processName", null);
                    domainObject.setProperty("viewName", null);
                    domainObject.setProperty("stored", false);

                    translations.put(locale, domainObject);
                }
            }
        }
    }


    /**
     * If we define a translation key "Qualifier:name", we say that there's a "Qualifier:name" key and a
     * unqualified "name" key. This adds the unqualified variants for qualified tags
     */
    private void fillInUnqualifiedReferences()
    {
        final Set<String> names = new HashSet<>(translations.keySet());
        for (String qualified : names)
        {
            int pos = qualified.lastIndexOf(':');
            if (pos >= 0)
            {
                String name = qualified.substring(pos + 1);
                addReference(name, ReferenceType.QUALIFIER, qualified);
            }
        }
    }


    /**
     *  Reads the translations existing in the database into the translation editor state.
     *  @param runtimeContext    runtime context
     *
     */
    private void loadDBTranslations(RuntimeContext runtimeContext)
    {
        final List<AppTranslation> list = translationProvider.provideTranslations(runtimeContext);
        for (AppTranslation appTranslation : list)
        {
            appTranslation.setDomainService(runtimeContext.getDomainService());
            addTranslations(appTranslation);
        }

    }


    /**
     * Adds translation references for domain types and domain properties.
     *  @param runtimeContext    runtime context
     *
     */
    private void addDomainNameReferences(RuntimeContext runtimeContext)
    {
        for (DomainType type : runtimeContext.getDomainService().getDomainTypes().values())
        {
            if (type.isSystem())
            {
                continue;
            }

            final String domainTypeName = type.getName();
            addReference(domainTypeName, ReferenceType.DOMAIN, domainTypeName);

            for (DomainProperty domainProperty : type.getProperties())
            {
                final String key = domainProperty.getTranslationTag();
                addReference(key, ReferenceType.DOMAIN, key);
            }
        }
    }


    /**
     * Adds the supported locale names as translation keys.
     *  @param runtimeContext    runtime context
     *
     */
    private void addLanguageReferences(RuntimeContext runtimeContext)
    {
        final List<String> supportedLocales = runtimeContext.getApplicationModel().getConfigModel().getSupportedLocales();
        for (String locale : supportedLocales)
        {
            addReference(locale, ReferenceType.LOCALE, locale);
        }
    }


    /**
     *  Adds translation references from components
     *  @param runtimeContext    runtime context
     *
     */
    private void addComponentReferences(RuntimeContext runtimeContext)
    {
        for (View view : runtimeContext.getApplicationModel().getViews().values())
        {
            final TranslationReferenceVisitor visitor = new TranslationReferenceVisitor();

            for (ComponentModel componentModel : view.getContent().values())
            {
                findTranslationRefs(visitor, componentModel);
            }

            for (String ref : visitor.getReferences())
            {
                addReference(ref, ReferenceType.VIEW, view.getName());
            }
        }
    }


    /**
     * Adds translation references from the static function references, i.e. static js calls find by js code inspection.
     *  @param runtimeContext    runtime context
     *
     */
    private void addModuleReferences(RuntimeContext runtimeContext)
    {
        final Map<String, ModuleFunctionReferences> refs =
            runtimeContext.getApplicationModel().getMetaData().getStaticFunctionReferences().getModuleFunctionReferences();

        for (Map.Entry<String, ModuleFunctionReferences> e : refs.entrySet())
        {
            for (String tag : e.getValue().getCalls(Translator.I18N_CALL_NAME))
            {
                addReference(tag, ReferenceType.MODULE, e.getKey());
            }
        }
    }


    /**
     * Recursively accepts the given visitor to all existing component attribute expressions and component attribute
     * default expressions.
     *
     * @param visitor       visitor
     * @param component     component
     */
    private void findTranslationRefs(TranslationReferenceVisitor visitor, ComponentModel component)
    {
        final Attributes attrs = component.getAttrs();
        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                final ExpressionValue attribute = attrs.getAttribute(name);
                if (attribute != null && attribute.getAstExpression() != null)
                {
                    attribute.getAstExpression().jjtAccept(visitor, null);
                }
            }
        }


        final ComponentRegistration componentRegistration = component.getComponentRegistration();
        if (componentRegistration != null)
        {
            for (PropDeclaration propDeclaration : componentRegistration.getDescriptor().getPropTypes().values())
            {
                if (
                    propDeclaration.getDefaultValue() != null &&
                        propDeclaration.getDefaultValue().getAstExpression() != null
                    )
                {
                    propDeclaration.getDefaultValue().getAstExpression().jjtAccept(visitor, null);
                }
            }
        }

        for (ComponentModel kid : component.children())
        {
            findTranslationRefs(visitor, kid);
        }
    }
}
