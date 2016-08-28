package de.quinscape.exceed.model.component;

import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.Map;

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

//    private final String version;


    public StaticFunctionReferences(
        @JSONParameter("usages")
        @JSONTypeHint(ModuleFunctionReferences.class)
        Map<String, ModuleFunctionReferences> usages
    )
    {
        this.usages = usages;
//        this.version = UUID.randomUUID().toString();
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

//    public String getVersion()
//    {
//        return version;
//    }


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
}
