package de.quinscape.exceed.runtime.component.translation;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.ModuleFunctionReferences;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.i18n.TranslationProvider;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.rtinfo.TranslationReferenceVisitor;
import de.quinscape.exceed.runtime.util.DomainUtil;
import de.quinscape.exceed.runtime.view.DataProviderContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a data for the translation editor which are:
 *
 * <ul>
 *     <li>
 *         A list of supported application locales
 *     </li>
 *     <li>
 *         translations that contains all existing translations and also instances prepared for the existing translation
 *         references in the system.
 *     </li>
 *     <li>
 *         All existing process and view names as locations for local translation rules.
 *     </li>
 * </ul>
 *
 *
 */
public class TranslationEditorProvider
    implements DataProvider
{
    private final TranslationProvider provider;


    public TranslationEditorProvider(TranslationProvider provider)
    {
        this.provider = provider;
    }


    @Override
    public Map<String, Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel,
                                       Map<String, Object> vars)
    {
        final RuntimeContext runtimeContext = dataProviderContext.getRuntimeContext();

        final TranslationEditorState state = new TranslationEditorState();

        convertAndInsertDBTranslations(runtimeContext, state);
        addModuleReferences(runtimeContext, state);
        addDomainNameReferences(runtimeContext, state);
        addLanguageReferences(runtimeContext, state);
        addComponentReferences(runtimeContext, state);

        final List<String> supportedLocales = runtimeContext.getApplicationModel().getSupportedLocales();

        Set<String> markedNew = state.initializeDefaults(runtimeContext, supportedLocales);

        return ImmutableMap.of(
            "supportedLocales", supportedLocales,
            "initialState", state.getTranslations(),
            "ruleLocations", locations(runtimeContext.getApplicationModel()),
            "markedNew", markedNew
        );
    }


    /**
     * Converts the translations existing in the database into the translation editor state.
     *
     * @param runtimeContext    runtime context
     * @param state             translation editor state
     */
    public void convertAndInsertDBTranslations(RuntimeContext runtimeContext, TranslationEditorState state)
    {
        final List<AppTranslation> list = provider.provideTranslations(runtimeContext);
        for (AppTranslation appTranslation : list)
        {
            appTranslation.setDomainService(runtimeContext.getDomainService());

            DomainObject converted = DomainUtil.convertToJSON(runtimeContext, appTranslation);

            state.addTranslations(converted);
        }

    }


    /**
     * Adds translation references for domain types and domain properties.
     *
     * @param runtimeContext    runtime context
     * @param state             translation editor state
     */
    public void addDomainNameReferences(RuntimeContext runtimeContext, TranslationEditorState state)
    {
        for (DomainType type : runtimeContext.getDomainService().getDomainTypes().values())
        {
            final String domainTypeName = type.getName();
            state.addReference(domainTypeName, ReferenceType.DOMAIN, domainTypeName);

            for (DomainProperty domainProperty : type.getProperties())
            {
                final String key = domainTypeName + ":" + domainProperty.getName();
                state.addReference(key, ReferenceType.DOMAIN, key);
            }
        }
    }


    /**
     * Adds the supported locale names as translation keys.
     *
     * @param runtimeContext    runtime context
     * @param state             translation editor state
     */
    public void addLanguageReferences(RuntimeContext runtimeContext, TranslationEditorState state)
    {
        final List<String> supportedLocales = runtimeContext.getApplicationModel().getSupportedLocales();
        for (String locale : supportedLocales)
        {
            state.addReference(locale, ReferenceType.LOCALE, locale);
        }
    }


    /**
     *  Adds translation references from components
     *
     * @param runtimeContext    runtime context
     * @param state             translation editor state
     */
    public void addComponentReferences(RuntimeContext runtimeContext, TranslationEditorState state)
    {
        for (View view : runtimeContext.getApplicationModel().getViews().values())
        {
            final TranslationReferenceVisitor visitor = new TranslationReferenceVisitor();
            findTranslationRefs(visitor, view.getRoot());
            for (String ref : visitor.getReferences())
            {
                state.addReference(ref, ReferenceType.VIEW, view.getName());
            }
        }
    }


    /**
     * Adds translation references from the static function references, i.e. static js calls find by js code inspection.
     *
     * @param runtimeContext    runtime context
     * @param state             translation editor state
     */
    public void addModuleReferences(RuntimeContext runtimeContext, TranslationEditorState state)
    {
        final Map<String, ModuleFunctionReferences> refs =
            runtimeContext.getApplicationModel().getStaticFunctionReferences().getModuleFunctionReferences();

        for (Map.Entry<String, ModuleFunctionReferences> e : refs.entrySet())
        {
            for (String tag : e.getValue().getCalls(Translator.I18N_CALL_NAME))
            {
                state.addReference(tag, ReferenceType.MODULE, e.getKey());
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
                final AttributeValue attribute = attrs.getAttribute(name);
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


    /**
     * Returns a list of rule locations for all existing views and processes.
     *
     * @param applicationModel  application model
     *
     * @return list of rule locations
     */
    private List<RuleLocation> locations(ApplicationModel applicationModel)
    {
        List<RuleLocation> locations = new ArrayList<>();
        for (String processName : applicationModel.getProcesses().keySet())
        {
            locations.add(new RuleLocation(processName, null));
        }

        for (View view : applicationModel.getViews().values())
        {
            if (view.isContainedInProcess())
            {
                locations.add(new RuleLocation(view.getProcessName(), view.getLocalName()));
            }
            else
            {
                locations.add(new RuleLocation(null, view.getName()));
            }
        }

        Collections.sort(locations, RuleLocationComparator.INSTANCE);
        return locations;
    }
}
