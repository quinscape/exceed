package de.quinscape.exceed.runtime.component;

import org.apache.commons.io.FileUtils;
import org.svenson.JSONParameter;
import org.svenson.JSONParser;
import org.svenson.JSONTypeHint;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Encapsulates the information contained within a <code>track-usage.json</code> file on the java side.
 *
 */
public class StaticFunctionReferences
{
    private final Map<String, ModuleFunctionReferences> usages;

    private final String version;


    public StaticFunctionReferences(
        @JSONParameter("usages")
        @JSONTypeHint(ModuleFunctionReferences.class)
        Map<String, ModuleFunctionReferences> usages
    )
    {
        this.usages = usages;
        this.version = UUID.randomUUID().toString();
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


    public String getVersion()
    {
        return version;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "usages = " + usages
            ;
    }
}
