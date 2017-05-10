package de.quinscape.exceed.model.meta;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.runtime.config.WebpackConfig;
import de.quinscape.exceed.runtime.i18n.Translator;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the information contained within a <code>track-usage.json</code> file on the java side.
 *
 * It contains the results of JavaScript code analysis that detects statically analyzable calls within the application
 * react components and other modules.
 *
 */
public class StaticFunctionReferences
{
    private final Map<String, ModuleFunctionReferences> usages;

    private final Set<String> editorTranslations;

    private final Set<String> docsTranslations;


    public StaticFunctionReferences(
        @JSONParameter("usages")
        @JSONTypeHint(ModuleFunctionReferences.class)
        Map<String, ModuleFunctionReferences> usages
    )
    {
        this.usages = usages;

        // determine the editor translation tag references for the current version
        this.editorTranslations = ImmutableSet.copyOf(
            collectReferencesFromModule(
            WebpackConfig.EDITOR_MAIN_MODULE,
            Translator.I18N_CALL_NAME,
            new HashSet<>(),
            new HashSet<>()
        ));

        // determine the editor translation tag references for the current version
        this.docsTranslations = ImmutableSet.copyOf(
            collectReferencesFromModule(
            WebpackConfig.DOCS_MAIN_MODULE,
            Translator.I18N_CALL_NAME,
            new HashSet<>(),
            new HashSet<>()
        ));
    }


    /**
     * Returns the module function references of a local module (with leading "./")
     *
     * @param module module name with leading "./"
     * @return ModuleFunctionReferences or null
     */
    public ModuleFunctionReferences getModuleFunctionReferences(String module)
    {
        return usages.get(module);
    }


    public Map<String, ModuleFunctionReferences> getModuleFunctionReferences()
    {
        return usages;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "usages = " + usages
            ;
    }


    /**
     * Collects the call references of the given call name in module with the given name and its dependencies.
     * 
     * @param moduleName            module name
     * @param callName              call name (e.g. {@link Translator#I18N_CALL_NAME })
     * @param collectedFirstArgs    collects the set of call arguments.
     * @param visited               collects the set of modules already visited.
     *
     * @return the collectedFirstArgs parameter
     */
    public Set<String> collectReferencesFromModule(String moduleName, String callName, Set<String> collectedFirstArgs, Set<String> visited)
    {
        if (!visited.contains(moduleName))
        {
            visited.add(moduleName);

            ModuleFunctionReferences refs = getModuleFunctionReferences(moduleName);
            if (refs != null)
            {
                List<String> calls = refs.getCalls(callName);
                if (calls != null && calls.size() != 0)
                {
                    collectedFirstArgs.addAll(calls);
                }

                for (String required : refs.getRequires())
                {
                    this.collectReferencesFromModule(required, callName, collectedFirstArgs, visited);
                }
            }
        }

        return collectedFirstArgs;
    }


    public Set<String> getEditorTranslations()
    {
        return editorTranslations;
    }


    public Set<String> getDocsTranslations()
    {
        return docsTranslations;
    }
}
