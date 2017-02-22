package de.quinscape.exceed.model.component;

import org.svenson.JSONParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the detected static function calls within one module.
 *
 */
public class ModuleFunctionReferences
{

    private final String module;

    private final List<String> requires;

    private final Map<String, List<String>> calls;


    public ModuleFunctionReferences(
        @JSONParameter("module")
        String module,
        @JSONParameter("requires")
        List<String> requires,
        @JSONParameter("calls")
        Map<String, List<String>> calls)
    {
        this.module = module;
        this.requires = requires;

        Map<String,Set<String>> map = new HashMap<>();
        this.calls = calls;
    }


    /**
     * Module name (without leading "./")
     * @return
     */
    public String getModule()
    {
        return module;
    }


    /**
     * Map of variable names mapping to
     * @return
     */
    public List<String> getRequires()
    {
        if (requires == null)
        {
            return Collections.emptyList();
        }
        return requires;
    }


    /**
     * Returns the list of call parameters for the given symbolic call name as defined in the babel plugin config.
     *
     * @param name
     * @return
     */
    public List<String> getCalls(String name)
    {
        List<String> calls = this.calls.get(name);
        if (calls == null)
        {
            return Collections.emptyList();
        }
        return calls;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "module = '" + module + '\''
            + ", requires = " + requires
            + ", calls = " + calls
            ;
    }
}
